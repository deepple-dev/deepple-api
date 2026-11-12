package kr.co.deepple.common.security;

import java.security.interfaces.RSAPublicKey;

public interface JwtPublicKeySource {
  RSAPublicKey getPublicKey();
}
