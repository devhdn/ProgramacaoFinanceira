package br.com.s3tech;

import java.math.BigDecimal;

import br.com.sankhya.extensions.regrasnegocio.ContextoRegra;
import br.com.sankhya.extensions.regrasnegocio.RegraNegocioJava;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import br.com.s3tech.helper.NotificacaoHelper;

public class ValidacaoPagamentoAvista implements RegraNegocioJava {

    @Override
    public void executa(ContextoRegra ctx) throws Exception {
        
        BigDecimal nunota = ctx.getNunota();
        if (nunota == null) return; 
        
        JapeWrapper cabecalhoNotaDAO = JapeFactory.dao("CabecalhoNota");
        DynamicVO notaVO = cabecalhoNotaDAO.findByPK(nunota);
        
        if (notaVO != null) {
            
            Integer codCondicaoPagamento = notaVO.asInt("CODTIPVENDA");
            
            if (codCondicaoPagamento != null && codCondicaoPagamento == 11) {
                
                NotificacaoHelper.enviarAvisoBloqueioVendaPorEvento(nunota, 1002);
                
                // 2. Bloqueia a ação na tela
                ctx.setSucesso(false);
                ctx.setMensagem("Operação bloqueada: Vendas com condição de pagamento à vista (Código 11) exigem verificação da gerência!");
                
            } else {
                 ctx.setSucesso(true);
            }
        }
    }
}