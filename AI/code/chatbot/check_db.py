import os

# 현재 디렉토리와 상위 디렉토리의 모든 .db 파일 찾기
def find_db_files():
    current_dir = os.path.dirname(os.path.abspath(__file__))
    parent_dir = os.path.dirname(current_dir)
    
    print("현재 작업 디렉토리:", os.getcwd())
    print("\n데이터베이스 파일 검색 결과:")
    
    for root, dirs, files in os.walk(parent_dir):
        for file in files:
            if file.endswith('.db'):
                full_path = os.path.join(root, file)
                print(f"발견된 DB 파일: {full_path}")
                print(f"파일 크기: {os.path.getsize(full_path)} bytes")

if __name__ == "__main__":
    find_db_files()