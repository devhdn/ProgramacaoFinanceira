package br.com.s3tech;

import java.math.BigDecimal;
import br.com.sankhya.extensions.regrasnegocio.ContextoRegra;
import br.com.sankhya.extensions.regrasnegocio.RegraNegocioJava;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.vo.DynamicVO;

public class ValidacaoPagamentoAvista implements RegraNegocioJava {

    @Override
    public void executa(ContextoRegra ctx) throws Exception {
        
        // 1. Obtém o número da nota (NUNOTA) a partir do contexto da execução
        BigDecimal nunota = ctx.getNunota();
        
        if (nunota == null) {
            return; // Prevenção de nulos: se o contexto não passar a nota, encerra.
        }

        // 2. Instancia o DAO da tabela de Cabeçalho de Nota (TGFCAB)
        JapeWrapper cabecalhoNotaDAO = JapeFactory.dao("CabecalhoNota");
        
        // 3. Busca o registro completo da nota no banco de dados
        DynamicVO notaVO = cabecalhoNotaDAO.findByPK(nunota);

        if (notaVO != null) {
            // 4. Pega o Tipo de Negociação / Condição de Pagamento (CODTIPVENDA)
            Integer codCondicaoPagamento = notaVO.asInt("CODTIPVENDA");

            // 5. Validação da sua regra: Se for o código 11 (À Vista), dispara a ação
            if (codCondicaoPagamento != null && codCondicaoPagamento == 11) {
                
                // O método mostraErro() interrompe a execução imediatamente e exibe um popup vermelho para o usuário
                ctx.mostraErro("Operação bloqueada: Vendas com condição de pagamento à vista (Código 11) exigem verificação e não podem prosseguir sem aprovação especial da gerência!");
                
            } else {
                // (Opcional) Se a validação passar, você pode definir uma mensagem de sucesso no rodapé ou popup verde
                // ctx.setMensagem("Validação de pagamento concluída com sucesso.");
            }
        }
    }
}