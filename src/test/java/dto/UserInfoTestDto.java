package dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoTestDto {

    private String nickname;
    private int level;
    private int score;
    private int solved;
    private int rank;

    @Builder
    public UserInfoTestDto(String nickname, int level, int score, int solved, int rank) {
        this.nickname = nickname;
        this.level = level;
        this.score = score;
        this.solved = solved;
        this.rank = rank;
    }

}
