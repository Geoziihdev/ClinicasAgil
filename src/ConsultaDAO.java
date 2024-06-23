import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsultaDAO {
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void salvarConsulta(Consulta consulta) {
        String sql = "INSERT INTO consultas(telefone_paciente, data_hora, especialidade) VALUES(?, ?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, consulta.getPaciente().getTelefone());
            pstmt.setString(2, consulta.getDataHora().format(formatter));
            pstmt.setString(3, consulta.getEspecialidade());
            pstmt.executeUpdate();
            System.out.println("Consulta marcada com sucesso!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean verificarDuplicidade(String telefone, LocalDateTime dataHora) {
        String sql = "SELECT id FROM consultas WHERE telefone_paciente = ? AND data_hora = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, telefone);
            pstmt.setString(2, dataHora.format(formatter));
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

	public List<Consulta> listarConsultas() {
		// TODO Auto-generated method stub
		return null;
	}

	public void fecharConexao() {
		// TODO Auto-generated method stub
		
	}
}
