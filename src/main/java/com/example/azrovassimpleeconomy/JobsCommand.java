package com.example.azrovassimpleeconomy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JobsCommand implements CommandExecutor, TabCompleter {

    private final AzrovasSimpleEconomy plugin;

    public JobsCommand(AzrovasSimpleEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("azrovaseconomy.jobs.access")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use job commands.");
            return true;
        }

        if (args.length == 0) {
            sendJobsHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                handleJoinJob(player, args);
                break;
            case "leave":
                handleLeaveJob(player);
                break;
            case "info":
                handleJobInfo(player, args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown jobs command. Use /jobs for help.");
                break;
        }
        return true;
    }

    private void handleJoinJob(Player player, String[] args) {
        if (!player.hasPermission("azrovaseconomy.jobs.join")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to join jobs.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /jobs join [job_name]");
            return;
        }
        JobType currentJob = plugin.getPlayerJob(player);
        if (currentJob != null) {
            player.sendMessage(ChatColor.RED + "You already have a job: " + currentJob.getDisplayName() + ". Please leave it first with /jobs leave.");
            return;
        }

        String jobName = args[1];
        JobType jobToJoin = JobType.fromString(jobName);

        if (jobToJoin == null) {
            player.sendMessage(ChatColor.RED + "Job '" + jobName + "' not found. Available jobs: Farmer, Miner, Lumberjack, Explorer, Fisherman.");
            return;
        }

        plugin.setPlayerJob(player, jobToJoin);
        player.sendMessage(ChatColor.GREEN + "You have joined the " + jobToJoin.getDisplayName() + " job!");
    }

    private void handleLeaveJob(Player player) {
        if (!player.hasPermission("azrovaseconomy.jobs.leave")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to leave jobs.");
            return;
        }
        JobType currentJob = plugin.getPlayerJob(player);
        if (currentJob == null) {
            player.sendMessage(ChatColor.RED + "You don't have a job to leave.");
            return;
        }
        plugin.setPlayerJob(player, null);
        player.sendMessage(ChatColor.GREEN + "You have left the " + currentJob.getDisplayName() + " job.");
    }

    private void handleJobInfo(Player player, String[] args) {
        if (!player.hasPermission("azrovaseconomy.jobs.info")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to view job info.");
            return;
        }
        JobType currentJob = plugin.getPlayerJob(player);
        if (currentJob != null) {
            player.sendMessage(ChatColor.GOLD + "Your current job: " + ChatColor.YELLOW + currentJob.getDisplayName());
            player.sendMessage(ChatColor.GRAY + "  " + currentJob.getDescription());
        }
        player.sendMessage(ChatColor.GOLD + "--- Available Jobs ---");
        for (JobType job : JobType.values()) {
            player.sendMessage(ChatColor.YELLOW + job.getDisplayName() + ChatColor.GRAY + " - " + job.getDescription());
            plugin.getAppConfig().getConfigurationSection("jobs.rewards." + job.name()).getKeys(false).stream().limit(3).forEach(action -> {
                double reward = plugin.getAppConfig().getDouble("jobs.rewards." + job.name() + "." + action);
                player.sendMessage(ChatColor.DARK_GRAY + "    - " + action + ": " + plugin.format(reward));
            });
             if(plugin.getAppConfig().getConfigurationSection("jobs.rewards." + job.name()).getKeys(false).size() > 3){
                player.sendMessage(ChatColor.DARK_GRAY + "    ...and more.");
            }
        }
    }

    private void sendJobsHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "--- Jobs Help ---");
        if (player.hasPermission("azrovaseconomy.jobs.join")) {
            player.sendMessage(ChatColor.YELLOW + "/jobs join [job_name]" + ChatColor.GRAY + " - Join a specific job.");
        }
        if (player.hasPermission("azrovaseconomy.jobs.leave")) {
            player.sendMessage(ChatColor.YELLOW + "/jobs leave" + ChatColor.GRAY + " - Leave your current job.");
        }
        if (player.hasPermission("azrovaseconomy.jobs.info")) {
            player.sendMessage(ChatColor.YELLOW + "/jobs info" + ChatColor.GRAY + " - View available jobs and your current job.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            if (sender.hasPermission("azrovaseconomy.jobs.join")) subCommands.add("join");
            if (sender.hasPermission("azrovaseconomy.jobs.leave")) subCommands.add("leave");
            if (sender.hasPermission("azrovaseconomy.jobs.info")) subCommands.add("info");
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            if (sender.hasPermission("azrovaseconomy.jobs.join")) {
                return Arrays.stream(JobType.values())
                        .map(JobType::name)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
} 