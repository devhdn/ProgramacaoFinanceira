package br.com.s3tech;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;
import com.sun.jmx.snmp.Timestamp;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.PlatformService;
import br.com.sankhya.modelcore.PlatformServiceFactory;
import br.com.sankhya.modelcore.PlatformServiceFactory.ServiceDescriptor;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ScheduleGeraEDI implements ScheduledAction {
       public void onTime(ScheduledActionContext contexto) {
              try {
                     // Para que seja possível preencher os parâmetros sem a interface com o usuário
                     // criamos uma colection e preenchemos essa colection com cada
                     // parâmetro de cada
                     // "registro" (chamo de registro a configuração de linha do arquivo).
                     Collection<Map<String, Object>> params = new ArrayList<Map<String, Object>>();
                     // O primeiro "registro" é o 1.02.00, ele usa duas variáveis: "DATA INICIO"
                     // e "DATA FINAL"
                     params.add(buildParam(10200, "DATA INICIO", "01/04/2016"));
                     params.add(buildParam(10200, "DATA FINAL", "30/04/2016"));
                     // O segundo "registro" é o 1.03.00, esse usa "DATData Inicial" e
                     // "DATData Inicial"
                     params.add(buildParam(10300, "DATData Inicial", "01/04/2016"));
                     params.add(buildParam(10300, "DATData Final", "30/04/2016"));
                     // e segue...
                     params.add(buildParam(10400, "DATData Inicial", "01/04/2016"));
                     params.add(buildParam(10400, "DATData Final", "30/04/2016"));
                     params.add(buildParam(10500, "DATData Inicial", "01/04/2016"));
                     params.add(buildParam(10500, "DATData Final", "30/04/2016"));
                     params.add(buildParam(10600, "DATData Inicial", "01/04/2016"));
                     params.add(buildParam(10600, "DATData Final", "30/04/2016"));
                     PlatformService ps = PlatformServiceFactory.getInstance().lookupService(
                                   "@core:edi.comercial.service");
                     ps.set("codLayout", new BigDecimal(10000));
                     ps.set("parametros", params);
                     ps.set("caminhoRepositorio", "IntercambioEletronico/relatorio10000");
                     ps.set("emails", "hamir@hamir.com.br,jose@jose.com.br");
                     /*
                      * Descomente se for usar FTP
                      * ps.set("caminhoFTP", "arquivos/edi/relatorio10000");
                      * ps.set("enderecoFTP", "ftp://meuftp.com.br");
                      * ps.set("usuarioFTP", "hamir");
                      * ps.set("senhaFTP", "123456");
                      */
                     ps.execute();
              } catch (Exception e) {
                     RuntimeException re = new RuntimeException(e);
                     throw re;
              }
       }

       // Esse método serve só pra agilizar a criação da Map, pois como cada parâmetro
       // precisa de 3 informações, ficaria extenso escrever linha a linha
       private Map<String, Object> buildParam(long codigo, String nome, Object valor) {
              Map<String, Object> param = new HashMap<String, Object>();
              param.put("codigo", new BigDecimal(codigo));
              param.put("nome", nome);
              param.put("valor", valor);
              return param;
       }
}