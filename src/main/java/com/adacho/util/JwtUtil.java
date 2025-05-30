package com.adacho.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${springboot.jwt.secret}")
    private String secretKey;

    // 토큰이 유효한지 검사
    public boolean isTokenValid(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .build();

            Jws<Claims> claims = parser.parseClaimsJws(token);

            Date expiration = claims.getBody().getExpiration();
            return expiration.after(new Date());
        } catch (ExpiredJwtException e) {
            System.out.println("토큰이 만료되었습니다.");
        } catch (UnsupportedJwtException e) {
            System.out.println("지원하지 않는 JWT입니다.");
        } catch (MalformedJwtException e) {
            System.out.println("잘못된 JWT입니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT가 비어있습니다.");
        }

        return false;
    }

    // 토큰에서 userId 추출
    public String getUserIdFromToken(String token) {
        JwtParser parser = Jwts.parserBuilder()
            .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
            .build();

        return parser.parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}
