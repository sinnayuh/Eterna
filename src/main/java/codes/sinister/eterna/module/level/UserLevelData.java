package codes.sinister.eterna.module.level;

import codes.sinister.eterna.util.database.UserProfile;
import org.jetbrains.annotations.NotNull;

public class UserLevelData {
    private static final String XP_KEY = "xp";
    private static final String LEVEL_KEY = "level";
    private static final int BASE_XP_REQUIRED = 100;
    private static final double LEVEL_SCALING = 1.5;

    private final UserProfile profile;
    private final String guildId;

    public UserLevelData(@NotNull UserProfile profile, @NotNull String guildId) {
        this.profile = profile;
        this.guildId = guildId;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public int getXp() {
        return profile.getGuildData(guildId, XP_KEY, 0);
    }

    public int getLevel() {
        return profile.getGuildData(guildId, LEVEL_KEY, 0);
    }

    public void setXp(int xp) {
        profile.updateGuildData(guildId, XP_KEY, xp);
    }

    public void setLevel(int level) {
        profile.updateGuildData(guildId, LEVEL_KEY, level);
    }

    /**
     * Add XP to the user and check for level up
     *
     * @param xpAmount amount to add
     * @return true if user leveled up, false otherwise
     */
    public boolean addXp(int xpAmount) {
        int currentXp = getXp();
        int currentLevel = getLevel();

        int newXp = currentXp + xpAmount;
        setXp(newXp);

        int xpRequired = getXpRequiredForLevel(currentLevel + 1);

        if (newXp >= xpRequired) {
            setLevel(currentLevel + 1);
            setXp(newXp - xpRequired);
            return true;
        }

        return false;
    }

    /**
     * Calculate XP required for next level
     */
    public int getXpRequiredForLevel(int level) {
        return (int) (BASE_XP_REQUIRED * Math.pow(level, LEVEL_SCALING));
    }

    /**
     * Get remaining XP needed to level up
     */
    public int getXpToNextLevel() {
        return getXpRequiredForLevel(getLevel() + 1) - getXp();
    }
}