import os
import json
import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
from transformers import AutoTokenizer, AutoModelForCausalLM, AutoConfig
import deepspeed
from typing import Optional, Dict
import logging
import sys
import signal
from huggingface_hub import hf_hub_download
from peft import get_peft_model, LoraConfig, TaskType


# 로깅 설정
logging.basicConfig(
    format='%(asctime)s - %(levelname)s - %(message)s',
    level=logging.INFO
)
logger = logging.getLogger(__name__)

# 글로벌 변수로 모델 엔진 저장
model_engine_global = None

# 시그널 핸들러
def signal_handler(sig, frame):
    logger.info("Signal received. Attempting to save model before exiting...")
    if model_engine_global is not None:
        try:
            save_path = os.path.join(args.output_dir, f"model_signal_save.pt")
            model_engine_global.save_checkpoint(args.output_dir, "signal_save")
            logger.info(f"Saved model checkpoint due to signal: {save_path}")
        except Exception as e:
            logger.error(f"Failed to save model during signal handling: {str(e)}")
    sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)

class RankingDataset(Dataset):
    def __init__(self, data_file: str, tokenizer, max_length: int = 512):
        self.samples = []
        self.tokenizer = tokenizer
        self.max_length = max_length

        try:
            with open(data_file, 'r', encoding='utf-8') as f:
                for line in f:
                    sample = json.loads(line)
                    self.samples.append(sample)
        except Exception as e:
            logger.error(f"Failed to load dataset: {str(e)}")
            raise e

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, idx: int) -> Dict:
        try:
            sample = self.samples[idx]
            query = sample['query']
            pos_doc = sample['positive_document']
            neg_doc = sample['negative_document']
            margin = sample['margin']

            query_enc = self.tokenizer(
                query,
                truncation=True,
                max_length=self.max_length,
                return_tensors='pt',
                padding='max_length'
            )
            pos_enc = self.tokenizer(
                pos_doc,
                truncation=True,
                max_length=self.max_length,
                return_tensors='pt',
                padding='max_length'
            )
            neg_enc = self.tokenizer(
                neg_doc,
                truncation=True,
                max_length=self.max_length,
                return_tensors='pt',
                padding='max_length'
            )

            return {
                'query_input_ids': query_enc['input_ids'].squeeze(0),
                'query_attention_mask': query_enc['attention_mask'].squeeze(0),
                'pos_input_ids': pos_enc['input_ids'].squeeze(0),
                'pos_attention_mask': pos_enc['attention_mask'].squeeze(0),
                'neg_input_ids': neg_enc['input_ids'].squeeze(0),
                'neg_attention_mask': neg_enc['attention_mask'].squeeze(0),
                'margin': torch.tensor(margin, dtype=torch.float)
            }
        except Exception as e:
            logger.error(f"Error processing sample {idx}: {str(e)}")
            raise e

class RankingModel(nn.Module):
    def __init__(self, model_name: str, access_token: Optional[str] = None, num_layers_to_keep: Optional[int] = None):
        super(RankingModel, self).__init__()
        try:
            self.model = AutoModelForCausalLM.from_pretrained(
                model_name, 
                token=access_token,
                low_cpu_mem_usage=True,
                torch_dtype=torch.float16
            )
            self.hidden_size = self.model.config.hidden_size

            # Layer-wise pruning: Keep only the lower `num_layers_to_keep` layers
            if hasattr(self.model, 'model'):
                transformer = self.model.model
                if hasattr(transformer, 'layers'):
                    total_layers = len(transformer.layers)
                    if num_layers_to_keep is None:
                        num_layers_to_keep = total_layers
                    if total_layers > num_layers_to_keep:
                        logger.info(f"Pruning model from {total_layers} layers to {num_layers_to_keep} layers.")
                        transformer.layers = nn.ModuleList(transformer.layers[:num_layers_to_keep])
                        transformer.config.num_hidden_layers = num_layers_to_keep
                    else:
                        logger.warning(f"Model has {total_layers} layers, which is less than or equal to {num_layers_to_keep}. No pruning applied.")
                else:
                    logger.error("The model does not have a 'transformer' attribute. Layer-wise pruning not applied.")
                    raise AttributeError("Model missing 'transformer' attribute for pruning.")

            # # LoRA
            # peft_config = LoraConfig(
            #     task_type=TaskType.CAUSAL_LM,
            #     inference_mode=False,
            #     r=8,
            #     lora_alpha=8,
            #     lora_dropout=0.1,
            #     target_modules=['q_proj', 'k_proj', 'v_proj', 'o_proj', "gate_proj", "up_proj", "down_proj"], # attention, MLP module
            # )
            # self.model = get_peft_model(self.model, peft_config)
            # logger.info(f"Model device map: {self.model.hf_device_map}")
            
            self.score_head = nn.Linear(self.hidden_size, 1)
        except Exception as e:
            logger.error(f"Failed to initialize model: {str(e)}")
            raise e

    def forward(self, query_input_ids, query_attention_mask,
                doc_input_ids, doc_attention_mask):
        try:
            # 쿼리 인코딩
            query_output = self.model(
                input_ids=query_input_ids,
                attention_mask=query_attention_mask,
                output_hidden_states=True,
                return_dict=True
            )
            query_hidden = query_output.hidden_states[-1][:, -1, :]
            # query_hidden = query_output.hidden_states[-1].mean(dim=1)

            # 문서 인코딩
            doc_output = self.model(
                input_ids=doc_input_ids,
                attention_mask=doc_attention_mask,
                output_hidden_states=True,
                return_dict=True
            )
            doc_hidden = doc_output.hidden_states[-1][:, -1, :]
            # doc_hidden = doc_output.hidden_states[-1].mean(dim=1)

            combined_rep = query_hidden * doc_hidden
            # combined_rep = torch.cat([
            #     query_hidden,
            #     doc_hidden,
            #     query_hidden * doc_hidden,
            #     torch.abs(query_hidden - doc_hidden)
            # ], dim=-1)
            scores = self.score_head(combined_rep)

            return scores
        except Exception as e:
            logger.error(f"Forward pass failed: {str(e)}")
            raise e
        #save_pretrained
    def save_pretrained(self, save_directory):
        """
        모델과 score_head를 함께 저장하는 메서드.
        """
        if not os.path.exists(save_directory):
            os.makedirs(save_directory)
        # HuggingFace 모델 저장
        self.model.save_pretrained(save_directory)
        # score_head 저장
        torch.save(self.score_head.state_dict(), os.path.join(save_directory, 'score_head.pt'))
        # 설정 파일 저장 (필요 시)
        config = self.model.config
        config.save_pretrained(save_directory)

    @classmethod
    def from_pretrained(cls, save_directory, access_token=None):
        """
        저장된 모델과 score_head를 로드하는 클래스 메서드.
        """
        # HuggingFace 모델 로드
        model = AutoModelForCausalLM.from_pretrained(save_directory, token=access_token)
        # score_head 로드
        # score_head_path = os.path.join(save_directory, 'score_head.pt')
        # score_head_state_dict = torch.load(score_head_path, map_location='cpu')

        try:
            score_head_path = hf_hub_download(
                repo_id=save_directory,
                filename='score_head.pt',
                token=access_token
            )
        except Exception as e:
            raise FileNotFoundError(f"Failed to download 'score_head.pt' from HuggingFace hub: {e}")

        # score_head 가중치 로드 (FutureWarning 해결을 위해 weights_only=True 설정)
        score_head_state_dict = torch.load(score_head_path, map_location='cpu', weights_only=True)
        # instance.score_head.load_state_dict(score_head_state_dict)
        
        # 모델 구성 로드
        config = AutoConfig.from_pretrained(save_directory)
        num_layers_to_keep = config.num_hidden_layers

        # RankingModel 인스턴스 생성
        ranking_model = cls(
            model_name=save_directory,
            access_token=access_token,
            num_layers_to_keep=num_layers_to_keep
        )
        # score_head 로드
        ranking_model.score_head.load_state_dict(score_head_state_dict)
        ranking_model.score_head = ranking_model.score_head.to(torch.float16)
        return ranking_model

def get_deepspeed_config(args):
    """DeepSpeed 설정 생성"""
    config = {
        "train_batch_size": args.batch_size * args.world_size,
        "gradient_accumulation_steps": 1,
        "optimizer": {
            "type": "AdamW",
            "params": {
                "lr": args.learning_rate,
                "betas": [0.9, 0.999],
                "eps": 1e-8,
                "weight_decay": 0.01
            }
        },
        "scheduler": {
            "type": "WarmupLR",
            "params": {
                "warmup_min_lr": 0,
                "warmup_max_lr": args.learning_rate,
                "warmup_num_steps": 100
            }
        },
        "zero_optimization": {
            "stage": 3,
            # "max_memory_offload": 8e9,
            "offload_param": {
                "device": "cpu",
                "pin_memory": True
                # "device": "none"
                
            },
            "offload_optimizer": {
                "device": "cpu",
                # "device": "none"
                "pin_memory": True
            },
            "stage3_max_live_parameters": 5e7,
            "stage3_max_reuse_distance": 5e7,
            "stage3_prefetch_bucket_size": 1e7,
            "stage3_param_persistence_threshold":1e4,
            "reduce_bucket_size": 1e7,
            "overlap_comm": True
        },
        "activation_checkpointing": {
            "partition_activations": True,
            "contiguous_memory_optimization": True,
            "cpu_checkpointing": True
        },
        "gradient_clipping": 1.0,
        "fp16": {
            "enabled": True,
            "loss_scale": 0,
            "loss_scale_window": 1000,
            "initial_scale_power": 16,
            "hysteresis": 2,
            "min_loss_scale": 1
        }
    }
    return config

def train(args):
    global model_engine_global
    try:
        # 환경 변수 설정
        os.environ['MASTER_ADDR'] = args.master_addr
        os.environ['MASTER_PORT'] = str(args.master_port)

        # 로컬 랭크 가져오기
        local_rank = int(os.environ.get("LOCAL_RANK", 0))

        # DeepSpeed 활성화
        deepspeed.init_distributed()

        # # DeepSpeed 체크포인팅 구성
        # deepspeed.checkpointing.configure(
        #     mpu=None,
        #     partition_activations=True,
        #     contiguous_checkpointing=True,
        #     checkpoint_in_cpu=True,
        #     synchronize=True,
        #     profile=False
        # )

        # 토크나이저 초기화
        tokenizer = AutoTokenizer.from_pretrained(args.model_name, token=args.access_token)
        tokenizer.pad_token = tokenizer.eos_token  # EOS 토큰을 패딩 토큰으로 사용

        # deepspeed 설정 가져오기
        ds_config = get_deepspeed_config(args)
        
        # 모델 초기화
        # with deepspeed.zero.Init(config_dict_or_path=ds_config):
        model = RankingModel(args.model_name, args.access_token, num_layers_to_keep=24)

        # 데이터셋 및 데이터로더 설정
        train_dataset = RankingDataset(args.train_data, tokenizer)
        train_sampler = torch.utils.data.distributed.DistributedSampler(
            train_dataset,
            num_replicas=args.world_size,
            rank=local_rank,
            shuffle=True,
            drop_last=True  # 모든 프로세스가 동일한 배치 수를 가지도록 설정
        )

        train_loader = DataLoader(
            train_dataset,
            batch_size=args.batch_size,
            sampler=train_sampler,
            num_workers=8,
            pin_memory=True,
            drop_last=True  # 모든 프로세스가 동일한 배치 수를 가지도록 설정
        )

        # DeepSpeed 엔진 초기화
        model_engine, optimizer, _, _ = deepspeed.initialize(
            args=args,
            model=model,
            model_parameters=model.parameters(),
            config=get_deepspeed_config(args)
        )

        # 글로벌 변수에 모델 엔진 저장
        model_engine_global = model_engine

        # 학습 루프
        for epoch in range(args.epochs):
            train_sampler.set_epoch(epoch)
            model_engine.train()

            for step, batch in enumerate(train_loader):
                try:
                    # 데이터를 현재 디바이스로 이동
                    batch = {k: v.to(model_engine.device) for k, v in batch.items()}

                    # Forward 패스
                    pos_scores = model_engine(
                        batch['query_input_ids'],
                        batch['query_attention_mask'],
                        batch['pos_input_ids'],
                        batch['pos_attention_mask']
                    )

                    neg_scores = model_engine(
                        batch['query_input_ids'],
                        batch['query_attention_mask'],
                        batch['neg_input_ids'],
                        batch['neg_attention_mask']
                    )

                    # 손실 계산
                    logits = pos_scores - neg_scores - batch['margin'].unsqueeze(1)
                    loss = -torch.log(torch.sigmoid(logits)).mean()

                    # Backward 패스
                    model_engine.backward(loss)
                    model_engine.step()

                    # 로깅
                    if local_rank == 0 and step % args.log_interval == 0:
                        logger.info(
                            f"Epoch [{epoch+1}/{args.epochs}], "
                            f"Step [{step}/{len(train_loader)}], "
                            f"Loss: {loss.item():.4f}"
                        )

                    # 주기적인 체크포인트 저장 (예: 매 1000 스텝마다)
                    if (step % 1000 == 0 and step != 0) or (step == 10):
                        checkpoint_dir = os.path.join(args.output_dir, f"checkpoint_epoch_{epoch+1}_step_{step}")
                        model_engine.save_checkpoint(checkpoint_dir, f"epoch_{epoch+1}_step_{step}")
                        if local_rank == 0:
                            logger.info(f"Saved periodic checkpoint: {checkpoint_dir}")

                except Exception as step_e:
                    logger.error(f"Error during training step {step}: {str(step_e)}")
                    # 현재까지의 모델 상태을 저장
                    if local_rank == 0:
                        try:
                            checkpoint_dir = os.path.join(args.output_dir, f"checkpoint_epoch_{epoch+1}_step_{step}_error")
                            model_engine.save_checkpoint(checkpoint_dir, f"epoch_{epoch+1}_step_{step}_error")
                            logger.info(f"Saved checkpoint due to error: {checkpoint_dir}")
                        except Exception as save_e:
                            logger.error(f"Failed to save checkpoint during error handling: {str(save_e)}")
                    raise step_e  # 예외 재발생

            # 에폭이 끝날 때 체크포인트 저장
            try:
                save_path = os.path.join(args.output_dir, f"model_epoch_{epoch+1}.pt")
                model_engine.save_checkpoint(args.output_dir, f"epoch_{epoch+1}")
                if local_rank == 0:
                    logger.info(f"Saved model checkpoint: {save_path}")
            except Exception as save_e:
                if local_rank == 0:
                    logger.error(f"Failed to save model checkpoint at epoch end: {str(save_e)}")

    except Exception as e:
        logger.error(f"Training failed with error: {str(e)}")
        raise e


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='DeepSpeed Training for Ranking Model')
    parser = deepspeed.add_config_arguments(parser)

    parser.add_argument('--train_data', type=str, default='./llama_ranker_dataset.jsonl', help='훈련 데이터 경로 (JSON Lines 형식)')
    # parser.add_argument('--model_name', type=str, default='meta-llama/Llama-3.1-8B-Instruct', help='모델 이름 또는 경로')
    parser.add_argument('--model_name', type=str, default='meta-llama/Llama-3.2-1B', help='모델 이름 또는 경로')
    parser.add_argument('--access_token', type=str, default='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', help='HuggingFace access token')
    parser.add_argument('--batch_size', type=int, default=1, help='배치 크기')
    parser.add_argument('--learning_rate', type=float, default=1e-5, help='학습률')
    parser.add_argument('--epochs', type=int, default=2, help='에폭 수')
    parser.add_argument('--world_size', type=int, default=3, help='총 GPU 수')
    parser.add_argument('--log_interval', type=int, default=10, help='로그 간격')
    parser.add_argument('--output_dir', type=str, default='./llama_ranker_model', help='모델 저장 디렉토리')
    parser.add_argument('--local_rank', type=int, default=0, help='Distributed training을 위한 로컬 랭크')
    parser.add_argument('--num_gpus', type=int, default=3, help='gpu 수')
    parser.add_argument('--master_addr', type=str, default='10.233.126.68', help='마스터 주소 (IP)')
    parser.add_argument('--master_port', type=int, default=29500, help='마스터 포트 번호')

    args = parser.parse_args()

    try:
        # 출력 디렉토리 생성
        if int(os.environ.get("LOCAL_RANK", 0)) == 0:
            os.makedirs(args.output_dir, exist_ok=True)

            # 로그 파일 설정
            log_file = os.path.join(args.output_dir, 'training.log')
            file_handler = logging.FileHandler(log_file)
            file_handler.setFormatter(logging.Formatter('%(asctime)s - %(levelname)s - %(message)s'))
            logger.addHandler(file_handler)

        # DeepSpeed 런처를 통해 학습 시작
        deepspeed.init_distributed()

        # GPU 캐시 정리
        torch.cuda.empty_cache()

        # 학습 시작
        train(args)

        if int(os.environ.get("LOCAL_RANK", 0)) == 0:
            logger.info("Training completed successfully")
            

    except Exception as e:
        logger.error(f"Training failed with error: {str(e)}")
        import traceback
        logger.error(traceback.format_exc())
        sys.exit(1)

    finally:
        # 정리 작업
        for handler in logger.handlers[:]:
            handler.close()
            logger.removeHandler(handler)

        if torch.cuda.is_available():
            torch.cuda.empty_cache()
