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
        
        BigDecimal nunota = ctx.getNunota();
        
        if (nunota == null) {
            return; 
        }
        
        JapeWrapper cabecalhoNotaDAO = JapeFactory.dao("CabecalhoNota");
        
        DynamicVO notaVO = cabecalhoNotaDAO.findByPK(nunota);

        if (notaVO != null) {
            
            Integer codCondicaoPagamento = notaVO.asInt("CODTIPVENDA");
            
            if (codCondicaoPagamento != null && codCondicaoPagamento == 11) {
                
                ctx.mostraErro("Operação bloqueada: Vendas com condição de pagamento à vista (Código 11) exigem verificação e não podem prosseguir sem aprovação especial da gerência!");
                
            } else {
                 ctx.setMensagem("Validação de pagamento concluída com sucesso.");
            }
        }
    }
}