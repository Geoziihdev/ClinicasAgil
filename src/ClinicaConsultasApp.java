import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ClinicaConsultasApp {
    private static final String URL = "jdbc:mysql://localhost:3306/ClinicaConsultasApp";
    private static final String USER = "root";
    private static final String PASSWORD = "sua_senha_mysql";

    private Connection conn;
    private Scanner scanner;
    private DateTimeFormatter formatter;

    public ClinicaConsultasApp() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão com o banco de dados estabelecida.");

            createTables(); // Método para criar as tabelas se não existirem
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco de dados: " + e.getMessage());
        }
        scanner = new Scanner(System.in);
        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    }

    public void run() {
        while (true) {
            exibirMenu();
            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1":
                    cadastrarPaciente();
                    break;
                case "2":
                    marcarConsulta();
                    break;
                case "3":
                    cancelarConsulta();
                    break;
                case "4":
                    listarConsultas();
                    break;
                case "5":
                    System.out.println("Encerrando o programa...");
                    fecharConexao();
                    return;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
        }
    }

    private void exibirMenu() {
        System.out.println("\n===== Clínica de Consultas Ágil =====");
        System.out.println("1. Cadastrar um paciente");
        System.out.println("2. Marcar consulta");
        System.out.println("3. Cancelar consulta");
        System.out.println("4. Listar consultas");
        System.out.println("5. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private void cadastrarPaciente() {
        System.out.print("Nome do paciente: ");
        String nome = scanner.nextLine();
        System.out.print("Telefone do paciente: ");
        String telefone = scanner.nextLine();

        if (buscarPacientePorTelefone(telefone) != null) {
            System.out.println("Paciente já cadastrado!");
            return;
        }

        String sql = "INSERT INTO pacientes (nome, telefone) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.setString(2, telefone);
            pstmt.executeUpdate();
            System.out.println("Paciente cadastrado com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar paciente: " + e.getMessage());
        }
    }

    private void marcarConsulta() {
        if (listarPacientes().isEmpty()) {
            System.out.println("Nenhum paciente cadastrado.");
            return;
        }

        listarPacientes();

        System.out.print("Escolha o número do paciente: ");
        int indicePaciente = Integer.parseInt(scanner.nextLine()) - 1;

        if (indicePaciente < 0 || indicePaciente >= listarPacientes().size()) {
            System.out.println("Opção inválida!");
            return;
        }

        String telefone = (String) listarPacientes().get(indicePaciente).get("telefone");
        System.out.print("Data e hora da consulta (DD/MM/YYYY HH:MM): ");
        String dataHoraString = scanner.nextLine();

        LocalDateTime dataHora;
        try {
            dataHora = LocalDateTime.parse(dataHoraString, formatter);
        } catch (Exception e) {
            System.out.println("Formato de data/hora inválido!");
            return;
        }

        if (dataHora.isBefore(LocalDateTime.now())) {
            System.out.println("Não é possível agendar consultas retroativas!");
            return;
        }

        if (verificarDuplicidadeConsulta(telefone, dataHora)) {
            System.out.println("Já existe uma consulta marcada para esse paciente na mesma data e hora!");
            return;
        }

        System.out.print("Especialidade da consulta: ");
        String especialidade = scanner.nextLine();

        String sql = "INSERT INTO consultas (telefone_paciente, data_hora, especialidade) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, telefone);
            pstmt.setString(2, dataHora.format(formatter));
            pstmt.setString(3, especialidade);
            pstmt.executeUpdate();
            System.out.println("Consulta marcada com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao marcar consulta: " + e.getMessage());
        }
    }

    private void cancelarConsulta() {
        List<Map<String, Object>> consultas = listarConsultas();

        if (consultas.isEmpty()) {
            System.out.println("Nenhuma consulta agendada.");
            return;
        }

        listarConsultas();

        System.out.print("Escolha o número da consulta para cancelar: ");
        int indiceConsulta = Integer.parseInt(scanner.nextLine()) - 1;

        if (indiceConsulta < 0 || indiceConsulta >= consultas.size()) {
            System.out.println("Opção inválida!");
            return;
        }

        int idConsulta = (int) consultas.get(indiceConsulta).get("id");

        String sql = "DELETE FROM consultas WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idConsulta);
            pstmt.executeUpdate();
            System.out.println("Consulta cancelada com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao cancelar consulta: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> listarPacientes() {
        List<Map<String, Object>> pacientes = new ArrayList<>();
        String sql = "SELECT * FROM pacientes";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> paciente = new HashMap<>();
                paciente.put("id", rs.getInt("id"));
                paciente.put("nome", rs.getString("nome"));
                paciente.put("telefone", rs.getString("telefone"));
                pacientes.add(paciente);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar pacientes: " + e.getMessage());
        }

        if (pacientes.isEmpty()) {
            System.out.println("Nenhum paciente cadastrado.");
        } else {
            System.out.println("\nPacientes cadastrados:");
            for (int i = 0; i < pacientes.size(); i++) {
                Map<String, Object> paciente = pacientes.get(i);
                System.out.println((i + 1) + ". " + paciente.get("nome") + " - " + paciente.get("telefone"));
            }
        }

        return pacientes;
    }

    private List<Map<String, Object>> listarConsultas() {
        List<Map<String, Object>> consultas = new ArrayList<>();
        String sql = "SELECT c.id, p.nome as nome_paciente, c.data_hora, c.especialidade " +
                     "FROM consultas c " +
                     "JOIN pacientes p ON c.telefone_paciente = p.telefone";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> consulta = new HashMap<>();
                consulta.put("id", rs.getInt("id"));
                consulta.put("nome_paciente", rs.getString("nome_paciente"));
                consulta.put("data_hora", rs.getString("data_hora"));
                consulta.put("especialidade", rs.getString("especialidade"));
                consultas.add(consulta);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar consultas: " + e.getMessage());
        }

        if (consultas.isEmpty()) {
            System.out.println("Nenhuma consulta agendada.");
        } else {
            System.out.println("\nConsultas agendadas:");
            for (int i = 0; i < consultas.size(); i++) {
                Map<String, Object> consulta = consultas.get(i);
                System.out.println((i + 1) + ". " + consulta.get("nome_paciente") + " - " +
                        consulta.get("data_hora") + " - " + consulta.get("especialidade"));
            }
        }

        return consultas;
    }

    private Paciente buscarPacientePorTelefone(String telefone) {
        String sql = "SELECT * FROM pacientes WHERE telefone = ?";
        Paciente paciente = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, telefone);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                paciente = new Paciente(rs.getString("nome"), rs.getString("telefone"));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar paciente: " + e.getMessage());
        }

        return paciente;
    }

    private boolean verificarDuplicidadeConsulta(String telefone, LocalDateTime dataHora) {
        String sql = "SELECT * FROM consultas WHERE telefone_paciente = ? AND data_hora = ?";
        boolean existeConsulta = false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, telefone);
            pstmt.setString(2, dataHora.format(formatter));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                existeConsulta = true;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar duplicidade de consulta: " + e.getMessage());
        }

        return existeConsulta;
    }

    private void createTables() throws SQLException {
        String sqlPacientes = "CREATE TABLE IF NOT EXISTS pacientes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "nome VARCHAR(100) NOT NULL," +
                "telefone VARCHAR(15) UNIQUE NOT NULL" +
                ")";
        String sqlConsultas = "CREATE TABLE IF NOT EXISTS consultas (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "telefone_paciente VARCHAR(15) NOT NULL," +
                "data_hora DATETIME NOT NULL," +
                "especialidade VARCHAR(100) NOT NULL," +
                "FOREIGN KEY (telefone_paciente) REFERENCES pacientes(telefone) ON DELETE CASCADE" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlPacientes);
            stmt.executeUpdate(sqlConsultas);
            System.out.println("Tabelas criadas com sucesso.");
        } catch (SQLException e) {
            throw new SQLException("Erro ao criar tabelas: " + e.getMessage());
        }
    }

    private void fecharConexao() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Conexão com o banco de dados fechada.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao fechar conexão com o banco de dados: " + e.getMessage());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    public static void main(String[] args) {
        ClinicaConsultasApp app = new ClinicaConsultasApp();
        app.run();
    }
}
