package org.zerock.Altari.Codef;

/**
 * <pre>
 * io.codef.easycodef
 *   |  EasyCodefClientInfo.java
 * </pre>
 *
 * Desc : EasyCodef 발급 클라이언트 정보 설정 클래스
 * @Company : ©CODEF corp.
 * @Author  : notfound404@codef.io
 * @Date    : Jun 26, 2020 3:42:11 PM
 * @Version : 1.0.1
 */
public class EasyCodefClientInfo {

    /**
     * TODO :	사용자는 코드에프 가입을 통해 발급 받은 클라이언트 정보와 RSA 공개키 정보를 설정해야 함.
     * 			설정하지 않은 상태에서는 SANDBOX 테스트만 사용 가능.
     */
    public static final String DEMO_CLIENT_ID = "0ea31620-2017-4461-bc22-96c37ee99af1";
    public static final String DEMO_CLIENT_SECRET = "8d166866-72eb-4621-92bc-7defbe2e7b64";

//	public static final String CLIENT_ID = "코드에프 데모 서비스 신청 후 발급 받은 정식버전 클라이언트 아이디 설정";
//	public static final String CLIENT_SECRET = "코드에프 데모 서비스 신청 후 발급 받은 정식버전 클라이언트 아이디 설정";
//
    /**  임시로 설정된 PUBLIC_KEY를 제거하고 코드에프 가입을 통해 발급 받은 본인 계정의 RSA 공개키 정보 설정 필요. */
    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlRbVakvQCFqijllWmCCVbAoVfVQq2+QOWO07aIHtr6Rhoq4EDfgVdgf4S4YdYSQm4fgNvqdT4wPYU0GulLDFy7c7DAyjvNNAOnoO6A+XmvQf6Q1vkGteUjBOJlfR1qVOYMXzd/yYMfH2GDED939tvh+6v2+1iZK9bBxD0b9FCZXShNBA/kQw8Ftv5w/YcJf9s/mpByn8IAbLPjOtTzi3n7YPAdbMgpRQfQxHbumua3zgQri+Doajmgr2pZGkVUIvEjW/Hb6I91VaAPSDfWnrwgkOeRoWEZ8ytpgcElCYL+l2/rAJNs4tq4aCHxTSpmQoI6rYZ0eoTv5sAue16FbNsQIDAQAB";

}