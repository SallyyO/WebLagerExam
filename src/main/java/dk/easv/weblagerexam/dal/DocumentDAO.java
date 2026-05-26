package dk.easv.weblagerexam.dal;

import dk.easv.weblagerexam.be.Box;
import dk.easv.weblagerexam.be.Document;
import dk.easv.weblagerexam.be.File;

import java.sql.*;
import java.util.ArrayList;
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

        String sql = "INSERT INTO [File] (file_number, document_id, is_barcode," +
                " file_data, is_deleted) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (File file : doc.getFiles()) {

                stmt.setInt(1, file.getFileNumber());
                stmt.setInt(2, doc.getId());
                stmt.setBoolean(3, file.isBarcode());
                stmt.setBytes(4, file.getImageData());
                stmt.setBoolean(5, false);

                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    file.setId(keys.getInt(1));
                }
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

    public File getLatestFileForUser(int userId) {
        String sql = """
                SELECT TOP 1 f.id, f.file_number, f.file_data, f.is_barcode, f.document_id
                FROM [File] f
                INNER JOIN Document d ON f.document_id = d.id
                INNER JOIN Box b      ON d.box_id       = b.id
                WHERE b.userId = ?
                ORDER BY f.id DESC
                """;

        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                File file = new File(rs.getBytes("file_data"), rs.getBoolean("is_barcode"));
                file.setId(rs.getInt("id"));
                file.setFileNumber(rs.getInt("file_number"));
                return file;
            }
            return null; // no previous scans

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Box getBoxForFile(int fileId) {
        String sql = """
                SELECT b.id, b.box_id, b.userId, b.profileId
                FROM Box b
                INNER JOIN Document d ON d.box_id    = b.id
                INNER JOIN [File] f   ON f.document_id = d.id
                WHERE f.id = ?
                """;

        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, fileId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Box box = new Box(rs.getInt("userId"));
                box.setId(rs.getInt("id"));
                box.setBoxId(rs.getInt("box_id"));
                box.setProfileId(rs.getInt("profileId"));
                return box;
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> getDocumentsForBox(int boxId) {
        String sql = """
            SELECT id, box_id, created_at
            FROM Document
            WHERE box_id = ?
            ORDER BY id ASC
            """;

        List<Document> documents = new ArrayList<>();
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, boxId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Document doc = new Document();
                doc.setId(rs.getInt("id"));
                doc.setBoxId(rs.getInt("box_id"));
                documents.add(doc);
            }
            return documents;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<File> getFilesForDocument(int documentId) {
        String sql = """
            SELECT id, file_number, is_barcode
            FROM [File]
            WHERE document_id = ?
            ORDER BY file_number ASC
            """;

        List<File> files = new ArrayList<>();
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, documentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                File file = new File(null, rs.getBoolean("is_barcode"));
                file.setId(rs.getInt("id"));
                file.setFileNumber(rs.getInt("file_number"));
                files.add(file);
            }
            return files;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public File getFileById(int fileId) {
        String sql = """
            SELECT id, file_number, is_barcode, file_data
            FROM [File]
            WHERE id = ?
            """;
        try (Connection con = conMan.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, fileId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                File file = new File(rs.getBytes("file_data"),
                        rs.getBoolean("is_barcode"));
                file.setId(rs.getInt("id"));
                file.setFileNumber(rs.getInt("file_number"));
                return file;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<File> getFilesForDocumentWithData(int documentId) {
        String sql = """
        SELECT id,
               file_number,
               is_barcode,
               file_data
        FROM [File]
        WHERE document_id = ?
        ORDER BY file_number
        """;

        List<File> files = new ArrayList<>();

        try (Connection con = conMan.getConnection()) {

            PreparedStatement stmt =
                    con.prepareStatement(sql);

            stmt.setInt(1, documentId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                File file = new File(
                        rs.getBytes("file_data"),
                        rs.getBoolean("is_barcode")
                );

                file.setId(rs.getInt("id"));
                file.setFileNumber(rs.getInt("file_number"));

                files.add(file);
            }

            return files;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}