package br.edu.ifbaiano.gpc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados de entrada para cadastrar ou alterar um produtor (POST/PUT). */
public record ProdutorRequestDTO(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank
        @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}|\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}",
                message = "CPF ou CNPJ inválido")
        String cpfCnpj,
        @Email @Size(max = 120) String email,
        @Size(max = 20) String telefone,
        @Size(max = 80) String municipio,
        @Pattern(regexp = "[A-Za-z]{2}", message = "UF deve ter 2 letras") String uf) {
}
