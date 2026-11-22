package br.edu.ifbaiano.gpc.dto;

import br.edu.ifbaiano.gpc.model.Produtor;

/**
 * Dados de saída do produtor. O CPF/CNPJ sai mascarado: a entidade completa
 * nunca é exposta pela API.
 */
public record ProdutorResponseDTO(
        Long id,
        String nome,
        String cpfCnpj,
        String email,
        String telefone,
        String municipio,
        String uf,
        int quantidadeTalhoes,
        double areaTotalHectares) {

    public static ProdutorResponseDTO de(Produtor p) {
        return new ProdutorResponseDTO(p.getId(), p.getNome(), mascarar(p.getCpfCnpj()),
                p.getEmail(), p.getTelefone(), p.getMunicipio(), p.getUf(),
                p.getTalhoes().size(), p.getAreaTotalHectares());
    }

    /** Mantém só os 3 primeiros e os 2 últimos dígitos. Ex.: 123.***.***-00 */
    static String mascarar(String documento) {
        if (documento == null) {
            return null;
        }
        String digitos = documento.replaceAll("\\D", "");
        if (digitos.length() < 5) {
            return "***";
        }
        String inicio = digitos.substring(0, 3);
        String fim = digitos.substring(digitos.length() - 2);
        return digitos.length() == 11
                ? inicio + ".***.***-" + fim
                : inicio.substring(0, 2) + ".***.***/****-" + fim;
    }
}
