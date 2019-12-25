package ru.beykerykt.android.filetransport.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ru.beykerykt.android.filetransport.data.AuthStatus;
import ru.beykerykt.android.filetransport.data.ServerStatus;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;
import ru.beykerykt.android.filetransport.database.utils.DatabaseHelper;

public class ServerManager {

    private SQLiteDatabase database;

    private DatabaseHelper dbHelper;

    private String[] allColumns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_NAME,
            DatabaseHelper.COLUMN_HOST,
            DatabaseHelper.COLUMN_PORT,
            DatabaseHelper.COLUMN_ANONYMOUS,
            DatabaseHelper.COLUMN_LOGIN,
            DatabaseHelper.COLUMN_PASSWORD,
            DatabaseHelper.COLUMN_ACTIVE_MODE
    };

    public ServerManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    private void close() {
        dbHelper.close();
    }

    private List<FTPServerEntity> getServersFromDB() {
        this.open();

        List<FTPServerEntity> results = new ArrayList<FTPServerEntity>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_SERVER,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FTPServerEntity server = this.cursorToServer(cursor);
            results.add(server);
            cursor.moveToNext();
        }

        cursor.close();
        this.close();

        return results;
    }

    private List<FTPServerEntity> servers;

    public List<FTPServerEntity> getServers() {
        if (servers == null) {
            servers = getServersFromDB();
        }
        return servers;
    }

    public FTPServerEntity getServerFromDB(int id) {
        this.open();

        FTPServerEntity result = null;

        Cursor cursor = database.query(DatabaseHelper.TABLE_SERVER,
                allColumns, DatabaseHelper.COLUMN_ID + "=" + id, null, null, null, null);

        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            result = this.cursorToServer(cursor);
        }

        cursor.close();
        this.close();

        return result;
    }

    public FTPServerEntity getServer(int id) {
        this.open();

        FTPServerEntity result = null;

        for (FTPServerEntity server : getServers()) {
            if (server.getId() == id) {
                result = server;
                break;
            }
        }

        this.close();

        return result;
    }

    public void addServer(FTPServerEntity server) {
        this.open();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_NAME, server.getName());
        values.put(DatabaseHelper.COLUMN_HOST, server.getHost());
        values.put(DatabaseHelper.COLUMN_PORT, server.getPort());
        values.put(DatabaseHelper.COLUMN_ANONYMOUS, server.isAnonymous());
        values.put(DatabaseHelper.COLUMN_LOGIN, server.getLogin());
        values.put(DatabaseHelper.COLUMN_PASSWORD, server.getPassword());
        values.put(DatabaseHelper.COLUMN_ACTIVE_MODE, server.isActiveMode());

        database.insert(DatabaseHelper.TABLE_SERVER, null, values);
        this.close();

        // add to local serverlist
        mLastId++;
        server.setId(mLastId);
        getServers().add(server);
    }

    public void updateServerToDB(FTPServerEntity server) {
        this.open();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_NAME, server.getName());
        values.put(DatabaseHelper.COLUMN_HOST, server.getHost());
        values.put(DatabaseHelper.COLUMN_PORT, server.getPort());
        values.put(DatabaseHelper.COLUMN_ANONYMOUS, server.isAnonymous());
        values.put(DatabaseHelper.COLUMN_LOGIN, server.getLogin());
        values.put(DatabaseHelper.COLUMN_PASSWORD, server.getPassword());
        values.put(DatabaseHelper.COLUMN_ACTIVE_MODE, server.isActiveMode());

        database.update(DatabaseHelper.TABLE_SERVER, values, DatabaseHelper.COLUMN_ID + "=" + server.getId(), null);
        this.close();
    }

    public void deleteServer(int serverId) {
        this.open();
        database.delete(DatabaseHelper.TABLE_SERVER, DatabaseHelper.COLUMN_ID + "=" + serverId, null);
        this.close();

        // remove to local serverlist
        getServers().remove(getServer(serverId));
    }

    private int mLastId = -1;

    private FTPServerEntity cursorToServer(Cursor cursor) {
        FTPServerEntity server = new FTPServerEntity();
        int id = cursor.getInt(0);
        if (id > mLastId) {
            mLastId = id;
        }
        server.setId(mLastId);
        server.setName(cursor.getString(1));
        server.setHost(cursor.getString(2));
        server.setPort(cursor.getInt(3));
        server.setIsAnonymous(cursor.getInt(4) > 0);
        server.setLogin(cursor.getString(5));
        server.setPassword(cursor.getString(6));
        server.setActiveMode(cursor.getInt(7) > 0);
        return server;
    }

    public void invalidateServerStatus() {
        for (FTPServerEntity entity : getServers()) {
            entity.authStatus = AuthStatus.AUTH_UNKNOWN;
            entity.serverStatus = ServerStatus.STATUS_UNKNOWN;
        }
    }
}
