package com.wire.bots.don;

import com.google.common.collect.ImmutableMultimap;
import com.wire.bots.don.db.Manager;
import com.wire.bots.don.db.User;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.Util;
import com.wire.bots.sdk.server.tasks.TaskBase;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;

public class ImportSqlV1 extends TaskBase {
    private final Configuration config;

    public ImportSqlV1(Configuration config) {
        super("import_sql_v1");
        this.config = config;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        File dir = new File(config.cryptoDir);
        if (!dir.exists()) {
            output.println("Dir: " + dir.getAbsolutePath() + " does not exist");
            return;
        }

        File[] botDirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return validateUUID(file.getName()) && file.isDirectory();
            }
        });

        Manager db = new Manager(config.cryptoDir + "/don.db");

        for (File botDir : botDirs) {
            final String botId = botDir.getName();

            try {
                output.printf("Checking botId %s ...\n", botId);

                File nameFile = new File(botDir.getPath() + "/don/.name");
                File emailFile = new File(botDir.getPath() + "/don/.email");
                File userFile = new File(botDir.getPath() + "/don/.user");
                File passwordFile = new File(botDir.getPath() + "/don/.password");
                File providerFile = new File(botDir.getPath() + "/don/.provider");

                if (!userFile.exists()) {
                    output.printf("\tMissing file: %s\n", userFile.getPath());
                    continue;
                }

                if (!nameFile.exists()) {
                    output.printf("\tMissing file: %s\n", nameFile.getPath());
                    continue;
                }

                String userId = Util.readLine(userFile);
                if (!validateUUID(userId)) {
                    output.printf("\tInvalid userId: %s in file: %s\n", userId, botDir.getPath() + "/don/.user");
                    continue;
                }

                String userName = Util.readLine(nameFile);

                User user = db.getUser(userId);
                if (user == null) {
                    int i = db.insertUser(userId, userName);
                    output.printf("\tinsertUser: %s %s res: %d\n", userId, userName, i);
                }

                // Email
                if (emailFile.exists()) {
                    String email = Util.readLine(emailFile);
                    if (validateEmail(email)) {
                        int u = db.updateUser(userId, "email", email);
                        output.printf("\tupdateUser Email: %s res: %d\n", email, u);
                    } else {
                        output.printf("\tInvalid Email: %s %s\n", email, emailFile.getPath());
                    }
                } else {
                    output.printf("\tMissing file: %s\n", emailFile.getPath());
                }

                // Password
                if (passwordFile.exists()) {
                    String password = Util.readLine(passwordFile);

                    int u = db.updateUser(userId, "password", password);
                    output.printf("\tupdateUser password: %s res: %d\n", password, u);
                } else {
                    output.printf("\tMissing file: %s\n", passwordFile.getPath());
                }

                // Provider
                if (providerFile.exists()) {
                    String providerId = Util.readLine(providerFile);
                    if (!validateUUID(providerId)) {
                        output.printf("\tInvalid providerId: %s in file: %s\n", providerId, botDir.getPath() + "/don/.provider");
                    }
                } else {
                    output.printf("\tMissing file: %s\n", providerFile.getPath());
                }
                output.printf("Imported: %s %s \n", userId, userName);
            } catch (Exception e) {
                String msg = String.format("Bot: %s %s", botId, e.getMessage());
                Logger.warning(msg);
                output.println(msg);
                e.printStackTrace();
            }
            output.flush();
        }
    }

    private boolean validateUUID(String uuid) {
        return uuid.split("-").length == 5;
    }

    private boolean validateEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

}
