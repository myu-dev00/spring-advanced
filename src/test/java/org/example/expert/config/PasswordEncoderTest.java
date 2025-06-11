package org.example.expert.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class PasswordEncoderTest {

    /*password 는 외부 의존성이 없는 클래스로 Mockito 어노테이션이 필요하지 않다.
    @InjectMocks
    private PasswordEncoder passwordEncoder;
*/
    PasswordEncoder passwordEncoder = new PasswordEncoder();

    @Test
    void matches_메서드가_정상적으로_동작한다() {
        // given
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // when
        boolean matches = passwordEncoder.matches(encodedPassword, rawPassword);

        // then
        assertTrue(matches);
    }
}
