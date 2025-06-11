package com.adacho.filter;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.adacho.exception.TokenExpiredException;
import com.adacho.util.JwtUtil;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException, java.io.IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (jwtUtil.isTokenValid(token)) {
                    // 토큰에서 사용자 정보 추출
                    String userId = jwtUtil.getUserIdFromToken(token);

                    // 인증 객체 생성 (예: UsernamePasswordAuthenticationToken)
                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of());

                    // SecurityContext에 등록
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }	
            }catch (TokenExpiredException e) {
            	sendErrorResponse(response, 403, e.getMessage());
            	return;
            }
        }
        filterChain.doFilter(request, response);
    }
    
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        try {
			response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
		} catch (java.io.IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

