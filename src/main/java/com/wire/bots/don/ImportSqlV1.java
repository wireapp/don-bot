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
                String email = Util.readLine(new File(botDir.getPath() + "/don/.email"));
                String userId = Util.readLine(new File(botDir.getPath() + "/don/.user"));
                String userName = Util.readLine(new File(botDir.getPath() + "/don/.name"));
                String password = Util.readLine(new File(botDir.getPath() + "/don/.password"));
                String providerId = Util.readLine(new File(botDir.getPath() + "/don/.provider"));

                output.printf("Checking botId %s ...\n", botId);
                if (!validateUUID(userId)) {
                    output.printf("Invalid userId: %s in file: %s\n", userId, botDir.getPath() + "/don/.user");
                    continue;
                }
                if (!validateUUID(providerId)) {
                    output.printf("Invalid providerId: %s in file: %s\n", providerId, botDir.getPath() + "/don/.provider");
                    continue;
                }
                if (!validateEmail(email)) {
                    output.printf("Invalid email: %s in file: %s\n", providerId, botDir.getPath() + "/don/.email");
                    continue;
                }

                output.printf("Importing: %s %s %s %s\n", userId, userName, email, providerId);

                User user = db.getUser(userId);
                if (user == null) {
                    int i = db.insertUser(userId, userName);
                    output.printf("\tinsertUser: res: %d\n", i);
                }
                int u = db.updateUser(userId, email, password, providerId);
                output.printf("\tupdateUser: res: %d\n", u);
                output.printf("Finished botId %s\n", botId);

                output.flush();
            } catch (Exception e) {
                String msg = String.format("Bot: %s %s", botId, e.getMessage());
                Logger.warning(msg);
                output.println(msg);
                output.flush();
                e.printStackTrace();
            }
        }
    }

    private boolean validateUUID(String uuid) {
        return uuid.split("-").length == 5;
    }

    private boolean validateEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

}
