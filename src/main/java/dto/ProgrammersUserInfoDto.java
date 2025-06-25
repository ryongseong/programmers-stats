package dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProgrammersUserInfoDto {

    private String nickname;
    private int level;
    private int score;
    private int solved;
    private int rank;

    @Builder
    public ProgrammersUserInfoDto(String nickname, int level, int score, int solved, int rank) {
        this.nickname = nickname;
        this.level = level;
        this.score = score;
        this.solved = solved;
        this.rank = rank;
    }

}