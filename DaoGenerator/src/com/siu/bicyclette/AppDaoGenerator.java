package com.siu.bicyclette;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class AppDaoGenerator {

    public static void main(String[] args) throws Exception {

        Schema schema = new Schema(2, "com.siu.bicyclette");
        schema.enableKeepSectionsByDefault();

        addStation(schema);
        addCity(schema);

        new DaoGenerator().generateAll(schema, "./src-gen");
    }

    private static void addStation(Schema schema) {

        Entity station = schema.addEntity("Station");
        station.implementsInterface("Parcelable");
        station.setTableName("stations");

        station.addIdProperty().columnName("_id");

        station.addStringProperty("name").columnName("name");
        station.addStringProperty("address").columnName("address");
        station.addStringProperty("city").columnName("city");

        station.addDoubleProperty("coordLat").columnName("lat");
        station.addDoubleProperty("coordLong").columnName("long");

        station.addIntProperty("total").columnName("total");
        station.addIntProperty("free").columnName("free");
        station.addIntProperty("available").columnName("available");

        station.addDateProperty("datetime").columnName("datetime");

        station.addBooleanProperty("open").columnName("open");
        station.addBooleanProperty("bonus").columnName("bonus");
    }

    private static void addCity(Schema schema) {

        Entity entity = schema.addEntity("City");
        entity.implementsInterface("Parcelable");
        entity.setTableName("cities");

        entity.addIdProperty().columnName("_id");

        entity.addStringProperty("name");

        entity.addDoubleProperty("coordLat");
        entity.addDoubleProperty("coordLong");
    }

}
