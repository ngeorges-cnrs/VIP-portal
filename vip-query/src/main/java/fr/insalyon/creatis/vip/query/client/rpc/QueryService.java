/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.insalyon.creatis.vip.query.client.rpc;

/**
 *
 * @author Boujelben
 */
import com.google.gwt.core.client.GWT;
import java.util.List;
import fr.insalyon.creatis.vip.query.client.bean.Query;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import fr.insalyon.creatis.vip.query.client.bean.QueryVersion;
import fr.insalyon.creatis.vip.query.client.view.QueryException;

public interface QueryService extends RemoteService {
    
   
public static final String SERVICE_URI = "/queryService";

    public static class Util {

        public static QueryServiceAsync getInstance() {

            QueryServiceAsync instance = (QueryServiceAsync) GWT.create(QueryService.class);
            ServiceDefTarget target = (ServiceDefTarget) instance;
            target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
            return instance;
        }
    }

    
    
    
    
    
     public List<Query> getQureies() throws QueryException;
     public List<String[]> getVersion() throws QueryException;
     public void add(Query query)throws QueryException;
     public void addVersion(QueryVersion version,Query query)throws QueryException;

    
}