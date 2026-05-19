package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.File;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileDAO
{
    ConnectionManager conMan = new ConnectionManager();

    public List<File> getAllFiles() {

        List<File> files = new ArrayList<>();

        String sql = """
                SELECT * FROM [File] ORDER BY id DESC
                """;

        try (Connection con = conMan.getConnection()) {

            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                File file = new File(null, rs.getBoolean("is_barcode"));

                file.setId(rs.getInt("id"));
                file.setFileNumber(rs.getInt("file_number"));
                file.setDeleted(rs.getBoolean("is_deleted"));

                files.add(file);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return files;
    }
    public List<File> searchFiles(String searchText) {

        List<File> files = new ArrayList<>();

        String sql = """
                SELECT id, file_number, is_barcode, is_deleted FROM [File] WHERE CAST(file_number AS VARCHAR) LIKE ? ORDER BY id DESC
                """;

        try (Connection con = conMan.getConnection()) {

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, "%" + searchText + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                File file = new File(null, rs.getBoolean("is_barcode"));

                file.setId(rs.getInt("id"));
                file.setFileNumber(rs.getInt("file_number"));
                file.setDeleted(rs.getBoolean("is_deleted"));

                files.add(file);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return files;
    }
    public void hardDeleteFile(int fileId) {

        String sql = "DELETE FROM [File] WHERE id = ?";

        try (Connection con = conMan.getConnection()) {

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, fileId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
