package codes.sinister.eterna;

public final class Main {
    public static void main(String[] args) {
        try {
            Bot bot = new Bot();
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                bot.shutdown();
                System.out.println("Bot has been shutdown gracefully");
            }));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}