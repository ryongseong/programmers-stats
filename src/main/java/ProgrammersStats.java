import service.BadgeService;

public class ProgrammersStats {

    private static final BadgeService badgeService = BadgeService.getBadgeService();

    public static void main(String[] args) {
        badgeService.generateBadge();
        System.out.println("✅ 배지 생성 완료!");
    }
}

