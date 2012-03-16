package com.siu.bicyclette;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class AppDaoGenerator {

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.siu.bicyclette");

        addStation(schema);

        new DaoGenerator().generateAll(schema, "./src-gen");
    }

    private static void addStation(Schema schema) {

        Entity note = schema.addEntity("Note");

        note.addIdProperty().columnName("_id");

        note.addStringProperty("name");
        note.addStringProperty("address");

        note.addDoubleProperty("lat");
        note.addDoubleProperty("long");

        note.addIntProperty("total");
        note.addIntProperty("free");
        note.addIntProperty("available");

        note.addDateProperty("datetime");

        note.addBooleanProperty("open");
        note.addBooleanProperty("bonus");
        note.addBooleanProperty("fav");
        note.addBooleanProperty("notif");
    }

}
