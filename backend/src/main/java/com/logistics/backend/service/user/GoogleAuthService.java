package com.logistics.backend.service.user;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.logistics.backend.entity.enums.Role;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.repository.user.RoleRepository;
import com.logistics.backend.repository.user.UserRepository;
import com.logistics.backend.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private static final String CLIENT_ID = "655789030334-vkbcqphi6i8hta26ev231f50ghgatlr7.apps.googleusercontent.com";
    private static final String WEB_CLIENT_ID = "655789030334-p3act2444oedviacebsf9ir8q0c092ct.apps.googleusercontent.com";
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public String authenticateWithGoogle(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Arrays.asList(CLIENT_ID, WEB_CLIENT_ID))
                .build();

        // Flutter'dan gelen Token'ı Google sunucularında doğrula
        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Kullanıcı bilgilerini Google'dan alıyoruz
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            User user = userRepository.findByEmail(email).orElse(null);

            // 3. Kullanıcı yoksa, yeni kullanıcı oluştur ve kaydet
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setName(name);
                user.setUsername(email); // UserDetailsServiceImpl'de email üzerinden arama yapıldığı için email'i atıyoruz

                // Kullanıcı Google ile giriş yaptığı için normal bir şifreye ihtiyacı yok,
                // ancak veritabanı kısıtlamalarına takılmamak için rastgele karmaşık bir şifre atıyoruz.
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

                user.setEmailVerified(true); // Google'dan geldiği için doğrudan onaylı kabul edebiliriz
                user.setEnabled(true);

                 user.setRole(roleRepository.findByRole(Role.CUSTOMER).orElse(null));

                // Yeni kullanıcıyı veritabanına kaydet
                user = userRepository.save(user);
            }

            // 4. Nakliyeciniz sistemine ait JWT'yi üret
            // Senin JwtUtils sınıfındaki özel metoda kullanıcının email'ini (username olarak) ve ID'sini gönderiyoruz
            String systemJwt = jwtUtils.generateTokenFromEmailAndId(user.getEmail(), user.getId());

            return systemJwt;
        } else {
            throw new Exception("Geçersiz Google Kimlik Jetonu!");
        }
    }
}
