import java.sql.Timestamp;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;

public class GerarTituloKm implements AcaoRotinaJava {
    
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        // Obtemos a consulta para buscar os lançamentos
        QueryExecutor query = contexto.getQuery();
        
        try {
            query.setParam("CODVEICULO", contexto.getParam("CODVEICULO"));
            // Buscando apenas os campos que serão usados, para melhor performance
            query.nativeSelect("SELECT SEQUENCIA, REEMBOLSO FROM AD_TADCKM WHERE CODVEICULO = {CODVEICULO}");
     
            double vlrDesdob = 0;
            while(query.next()){
                double reembolso = query.getDouble("REEMBOLSO");
     
                // Só permitimos gerar o título quando todos os lançamentos tiverem reembolso
                if(reembolso > 0){
                    vlrDesdob += reembolso;
                } else {
                    contexto.mostraErro("O reembolso do lançamento " + query.getInt("SEQUENCIA") + " não foi calculado ainda.");
                }
            }
     
            if(vlrDesdob == 0){
                contexto.confirmar("Valor do título zerado", "O veículo informado não possui lançamentos para reembolso, o título terá valor zerado. Deseja continuar?", 1);
            }
            
        } finally {
            // O try/finally garante que a query SEMPRE seja fechada, mesmo se ocorrer um erro no while
            query.close();
        }
 
        // Captura a data/hora atual do servidor
        Timestamp dataAtual = new Timestamp(System.currentTimeMillis());

        // Solicitamos a inclusão de uma linha no financeiro
        Registro financeiro = contexto.novaLinha("TGFFIN");
 
        // Inserindo os dados do título
        financeiro.setCampo("VLRDESDOB", vlrDesdob);
        financeiro.setCampo("RECDESP", -1); // -1 = Despesa
        financeiro.setCampo("CODEMP", 11);  // ATENÇÃO: Considere passar por parâmetro
        financeiro.setCampo("NUMNOTA", 0);
        financeiro.setCampo("DTNEG", dataAtual); 
        financeiro.setCampo("CODPARC", 0); 
        financeiro.setCampo("CODNAT", 3050200); // ATENÇÃO: Considere passar por parâmetro
        financeiro.setCampo("CODBCO", 0);
        financeiro.setCampo("CODTIPTIT", 2);
        financeiro.setCampo("DTVENC", dataAtual); // Vencimento para o mesmo dia
        financeiro.setCampo("HISTORICO", "REEMBOLSO DE KM PARA O VEÍCULO " + contexto.getParam("CODVEICULO"));
 
        // Salva o registro no banco de dados
        financeiro.save();
 
        // Mensagem de retorno
        String nufin = financeiro.getCampo("NUFIN").toString();
        String veiculo = contexto.getParam("CODVEICULO").toString();
        
        contexto.setMensagemRetorno(String.format("Foi gerado o título %s no valor de R$ %.2f como reembolso de KM para o veículo %s.", nufin, vlrDesdob, veiculo));
    }
}