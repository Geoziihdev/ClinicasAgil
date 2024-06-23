import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PacienteDAO {
    private Connection conn;

    public PacienteDAO() {
        conn = Database.connect();
        if (conn == null) {
            System.out.println("Erro ao conectar ao banco de dados.");
            System.exit(1); // Encerra o programa em caso de falha na conexão
        }
    }

    public void fecharConexao() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println("Erro ao fechar a conexão com o banco de dados: " + e.getMessage());
        }
    }

    public void salvarPaciente(Paciente paciente) {
        String sql = "INSERT INTO pacientes(nome, telefone) VALUES(?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, paciente.getNome());
            pstmt.setString(2, paciente.getTelefone());
            pstmt.executeUpdate();
            System.out.println("Paciente cadastrado com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar paciente: " + e.getMessage());
        }
    }

    public Paciente buscarPacientePorTelefone(String telefone) {
        String sql = "SELECT nome, telefone FROM pacientes WHERE telefone = ?";
        Paciente paciente = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, telefone);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String nome = rs.getString("nome");
                paciente = new Paciente(nome, telefone);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar paciente: " + e.getMessage());
        }

        return paciente;
    }

    public Map<String, Paciente> listarPacientes() {
        String sql = "SELECT nome, telefone FROM pacientes";
        Map<String, Paciente> pacientes = new HashMap<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String nome = rs.getString("nome");
                String telefone = rs.getString("telefone");
                Paciente paciente = new Paciente(nome, telefone);
                pacientes.put(telefone, paciente);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar pacientes: " + e.getMessage());
        }

        return pacientes;
    }
}
