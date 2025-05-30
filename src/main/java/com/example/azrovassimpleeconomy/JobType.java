package com.example.azrovassimpleeconomy;

public enum JobType {
    FARMER("Farmer", "Harvests crops for a living."),
    MINER("Miner", "Mines ores and valuable minerals."),
    LUMBERJACK("Lumberjack", "Chops down trees for wood."),
    EXPLORER("Explorer", "Travels the world and discovers new places."),
    FISHERMAN("Fisherman", "Catches fish from various water sources.");

    private final String displayName;
    private final String description;

    JobType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static JobType fromString(String jobName) {
        for (JobType jobType : JobType.values()) {
            if (jobType.name().equalsIgnoreCase(jobName) || jobType.getDisplayName().equalsIgnoreCase(jobName)) {
                return jobType;
            }
        }
        return null;
    }
} 