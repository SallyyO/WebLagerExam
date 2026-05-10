package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.Document;
import dk.easv.weblagerexam.be.File;

import java.sql.*;
import java.util.List;

public class DocumentDAO {


    ConnectionManager conMan = new ConnectionManager();

    public int saveDocument(Document doc) {
        String sql = "INSERT INTO Document (box_id, created_at) VALUES (?, SYSDATETIME())";

        try (Connection con = conMan.getConnection()) {

            con.setAutoCommit(false); // We want db transactions to make sure we don't have stuff lying around

            try {

                PreparedStatement stmt =
                        con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, doc.getBoxId());
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();

                if (!keys.next()) {
                    throw new RuntimeException("No document ID generated");
                }

                int generatedId = keys.getInt(1);
                doc.setId(generatedId);
                saveFiles(con, doc);
                con.commit();
                return generatedId;

            } catch (Exception e) {
                con.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void saveFiles(Connection con, Document doc) {

        String sql = "INSERT INTO [File] (file_name, file_number, document_id,is_barcode," +
                " file_data, is_deleted) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            for (File file : doc.getFiles()) {

                stmt.setString(1, file.getFileName());
                stmt.setInt(2, file.getFileNumber());
                stmt.setInt(3, doc.getId());
                stmt.setBoolean(4, file.isBarcode());
                stmt.setBytes(5, file.getImageData());
                stmt.setBoolean(6, false);
                stmt.addBatch();
            }

            stmt.executeBatch();

            // Read generated IDs so file.getId() works for updateFileOrder later
            ResultSet keys = stmt.getGeneratedKeys();
            List<File> files = doc.getFiles();
            int i = 0;
            while (keys.next() && i < files.size()) {
                files.get(i).setId(keys.getInt(1));
                i++;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateMetadata(int documentId, String metadata) {
        String sql = "UPDATE Document SET metadata = ? WHERE id = ?";

        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, metadata);
            stmt.setInt(2, documentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getTotalDocumentCount() {
        String sql = "SELECT COUNT(*) FROM Document";

        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateFileOrder(Document doc) {
        String sql = "UPDATE [File] SET file_number = ? WHERE document_id = ? AND id = ?";

        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            for (File file : doc.getFiles()) {
                stmt.setInt(1, file.getFileNumber());
                stmt.setInt(2, doc.getId());
                stmt.setInt(3, file.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}