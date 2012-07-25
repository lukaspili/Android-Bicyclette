package com.siu.bicyclette;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class DaoGenerator {

    public static void main(String[] args) throws Exception {

        Schema schema = new Schema(1, "com.siu.android.bicyclette");
        schema.enableKeepSectionsByDefault();

        Entity station = schema.addEntity("Station");
        station.implementsSerializable();
        station.setTableName("stations");

        station.addIdProperty();
        station.addIntProperty("remote_id");

        station.addStringProperty("name");
        station.addStringProperty("address");
        station.addStringProperty("city");

        station.addDoubleProperty("latitude").columnName("lat");
        station.addDoubleProperty("longitude").columnName("long");

        station.addIntProperty("total");
        station.addIntProperty("free");
        station.addIntProperty("available");

        station.addDateProperty("datetime").columnName("datetime");

        station.addBooleanProperty("open").columnName("open");
        station.addBooleanProperty("bonus").columnName("bonus");

        new de.greenrobot.daogenerator.DaoGenerator().generateAll(schema, "./src-gen");
    }

//    private static void addCity(Schema schema) {
//
//        Entity entity = schema.addEntity("City");
//        entity.implementsInterface("Parcelable");
//        entity.setTableName("cities");
//
//        entity.addIdProperty().columnName("_id");
//
//        entity.addStringProperty("name");
//
//        entity.addDoubleProperty("coordLat");
//        entity.addDoubleProperty("coordLong");
//    }

}
