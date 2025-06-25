package dto;

import lombok.Getter;

@Getter
public class ProgrammersLoginDto {

    private final User user;

    public ProgrammersLoginDto(String email, String password) {
        this.user = new User(email, password);
    }

    @Getter
    class User {
        private String email;
        private String password;

        public User(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}
