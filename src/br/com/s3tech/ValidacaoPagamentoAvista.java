package br.com.s3tech;

import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.core.JapeSession;

public class ValidacaoPagamentoAvista implements Regra {

    @Override
    public void beforeUpdate(ContextoRegra ctx) throws Exception {
        // 1. Pegamos a nota atual sendo modificada
        DynamicVO notaVO = (DynamicVO) ctx.getPrePersistEntityState().getNewVO();
        
        // 2. Verificamos se é o momento exato da confirmação da nota
        Boolean isConfirmando = (Boolean) JapeSession.getProperty("CabecalhoNota.confirmando.nota");
        if (!Boolean.TRUE.equals(isConfirmando)) return; // Se não for confirmação, sai da regra.

        // 3. Exemplo de lógica: Pega o código da condição de pagamento (hipotético)
        Integer codCondicaoPagamento = notaVO.asInt("CODTIPVENDA"); 
        
        // Se a condição for à vista (ex: código 1) e houver alguma violação...
        if (codCondicaoPagamento != null && codCondicaoPagamento == 11) {
            
            // Você pode lançar uma exceção para barrar a confirmação:
            // throw new Exception("Vendas à vista precisam de aprovação especial nesta filial!");
            
            // OU pode mandar um aviso não bloqueante para a tela:
            ctx.getBarramentoRegra().addMensagem("Lembrete: Verifique o comprovante do PIX para esta venda à vista.");
        }
    }

    // ... os outros métodos podem ficar vazios (sem o throw) se não forem usados ...
    @Override public void afterUpdate(ContextoRegra ctx) throws Exception {}
    @Override public void beforeInsert(ContextoRegra ctx) throws Exception {}
    @Override public void afterInsert(ContextoRegra ctx) throws Exception {}
    @Override public void beforeDelete(ContextoRegra ctx) throws Exception {}
    @Override public void afterDelete(ContextoRegra ctx) throws Exception {}
}