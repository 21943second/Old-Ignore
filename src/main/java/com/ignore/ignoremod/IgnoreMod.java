package com.ignore.ignoremod;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.util.text.StringTextComponent; import java.awt.*; import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("ignoremod")
public class IgnoreMod
{

    public IgnoreMod() {
//        // Register the setup method for modloading
//        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
//        // Register the enqueueIMC method for modloading
//        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
//        // Register the processIMC method for modloading
//        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        loadConfig();
    }

    private ArrayList<String> ignoredNames = new ArrayList<>();
    private ArrayList<String> ignoredWords = new ArrayList<>();

    @SubscribeEvent
    public void onClientChatEvent(ClientChatEvent event) throws IOException {
        // This is me sending a message
        String message = event.getMessage().toLowerCase();
        String command = message.split(" ")[0].replace("/", "");
        message = message.replace("/" + command, "").trim();
        if(command.equals("ignorehelp")) {
            sendUserMessage("Ignore mod instructions...");
            sendUserMessage("\"/ignorehelp\" -> Prints this help message");
            sendUserMessage("\"/ignoreuser username\" -> Adds a username to the ignore list");
            sendUserMessage("\"/unignoreuser username\" -> Removes a username from the ignore list");
            sendUserMessage("\"/ignoreword word\" -> Adds a word to the ignore list");
            sendUserMessage("\"/unignoreword word\" -> Removes a word from the ignore list");
            sendUserMessage("\"/ignorelist\" -> Prints all ignored usernames and words");
            sendUserMessage("\"/ignoreuserlist\" -> Prints all ignored usernames");
            sendUserMessage("\"/ignorewordlist\" -> Prints all ignored words");
        } else if(command.equals("ignoreuser")) {
            ignore("names", ignoredNames, message);
            save(ignoredNames, "ignoredNames");
        } else if (command.equals("unignoreuser")) {
            ignoredNames = unignore("names", ignoredNames, message);
            save(ignoredNames, "ignoredNames");
        } else if (command.equals("ignoreword")) {
            ignore("words", ignoredWords, message);
            save(ignoredWords, "ignoredWords");
        } else if (command.equals("unignoreword")) {
            ignoredWords = unignore("words", ignoredWords, message);
            save(ignoredWords, "ignoredWords");
        } else if (command.equals("ignorelist")) {
            printIgnoredUsers();
            printIgnoredWords();
        } else if (command.equals("ignoreuserlist")) {
            printIgnoredUsers();
        } else if (command.equals("ignorewordlist")) {
            printIgnoredWords();
        } else if (command.equals("ignoresave")) {
            saveConfig();
            sendUserMessage("Manually saved config");
        } else if (command.equals("ignoreload")) {
            loadConfig();
            sendUserMessage("Manually loaded config");
        } else {
            return;
        }
        event.setCanceled(true);
    }

    private void printIgnoredUsers() {
        String ignoredNamesMessage = "List of ignored names: " + String.join(", ", ignoredNames);
        sendUserMessage(ignoredNamesMessage);
    }

    private void printIgnoredWords() {
        String ignoredWordsMessage = "List of ignored words: " + String.join(", ", ignoredWords);
        sendUserMessage(ignoredWordsMessage);
    }

    // TODO: Allow mult seperated by comma
    private void ignore(String ignoreType, ArrayList ignoreList, String input) {
        if(ignoreList.contains(input)) {
            sendUserMessage(input + " is already in the list of ignored " + ignoreType + ".");
            return;
        }
        ignoreList.add(input);
        sendUserMessage("Added " + input + " to the list of ignored " + ignoreType + ".");
    }

    private ArrayList<String> unignore(String ignoreType, ArrayList ignoreList, String input) {
        if(!ignoreList.contains(input)) {
            sendUserMessage(input + " is not in the list of ignored " + ignoreType + ".");
            return ignoreList;
        }
        sendUserMessage(input + " has been removed from the list of ignored " + ignoreType + ".");
        return (ArrayList<String>) ignoreList
                .stream()
                .filter(name -> !name.equals(input))
                .collect(Collectors.toList());
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        // This is someone else sending a message
        String message = event.getMessage().getString().toLowerCase();

        if(!message.contains(":")) return;

        String sender = message.split(":")[0];
        if (sender.contains(".")) {
            sender = sender.split("\\.")[1];
        } else if (sender.contains("<")) {
            sender = sender.split("<")[1];
        } else if (sender.contains("]")) {
            sender = sender.split("] ")[1];
        }

        if (ignoredNames.contains(sender)) {
            System.out.println("Cancelled message \"" + event.getMessage().getString() + "\" because it was sent by \"" + sender + "\".");
            event.setCanceled(true);
        }

        String content = message.substring(message.indexOf(":") + 2);

        for(String word : ignoredWords) {
            if (content.contains(word)) {
                System.out.println("Cancelled message \"" + event.getMessage().getString() + "\" because it contained \"" + word + "\".");
                event.setCanceled(true);
            }
        }
        System.out.println("Allowed message \"" + event.getMessage().getString() + "\"");
    }

    // Works
    private static void sendUserMessage(String msg) {
        ITextComponent m = new StringTextComponent("§4" + msg);
        Minecraft.getInstance().player.sendMessage(m);
    }

    private void loadConfig() {
        ignoredNames = load("ignoredNames");
        ignoredWords = load("ignoredWords");
    }

    private void saveConfig() {
        save(ignoredNames, "ignoredNames");
        save(ignoredWords, "ignoredWords");
    }

    private void save(ArrayList list, String location) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("config/" + location + ".save");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(fos);
            oos.writeObject(list);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> load(String location) {
        FileInputStream fis = null;
        if(new File("config/"+location+".save").isFile()) {
            try {
                fis = new FileInputStream("config/" + location + ".save");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                try {
                    return (ArrayList<String>) ois.readObject();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<String>();
    }

}
