package br.edu.ifbaiano.gpc.command;

import br.edu.ifbaiano.gpc.model.Safra;

/**
 * Padrão GoF Command: cada operação do ciclo de vida da safra (sugerir plantio,
 * plantar, colher, cancelar) é encapsulada em um objeto com o método {@link #executar}.
 *
 * <ul>
 *   <li><b>Comando concreto:</b> as classes deste pacote;</li>
 *   <li><b>Receptor:</b> a entidade {@link Safra}, que contém a regra de cada transição;</li>
 *   <li><b>Invocador:</b> {@link ExecutorComandoSafra}, que carrega a safra, abre a
 *       transação, executa o comando e registra o histórico;</li>
 *   <li><b>Cliente:</b> o {@code SafraService}, que cria o comando com os parâmetros.</li>
 * </ul>
 *
 * @param <R> tipo do resultado devolvido à API
 */
public interface ComandoSafra<R> {

    /** Nome curto do comando, usado no histórico (ex.: "PLANTAR"). */
    String nome();

    R executar(Safra safra);
}
