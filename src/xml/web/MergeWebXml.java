package xml.web;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.transform.RegistryMatcher;

import xml.web.transform.JspFileTypeTransform;
import xml.web.transform.ServletClassTypeTransform;
import xml.web.transform.ServletNameTypeTransform;
import xml.web.transform.UrlPatternTypeTransform;
import xml.web.type.JspFileType;
import xml.web.type.ServletClassType;
import xml.web.type.ServletNameType;
import xml.web.type.UrlPatternType;
import xml.web.valueobject.ContextParam;
import xml.web.valueobject.Filter;
import xml.web.valueobject.FilterMapping;
import xml.web.valueobject.Listener;
import xml.web.valueobject.Servlet;
import xml.web.valueobject.ServletMapping;
import xml.web.valueobject.WebApp;

/**
 * 
 * @author Mark Bernard
 */
public class MergeWebXml {
    private String srcfile1;
    private String srcfile2;
    private String destfile;
    private String conflictfile;

    /**
     * Create work object. srcfile1 and srcfile2 will be merged into desfile. 
     * All conflicting entries will be writtend to conflictfile.
     * If conflict file is null then all conflicting entries will be silently dropped.
     * 
     * @param srcfile1 First web.xml file to merge. Should be the full path to the file.
     * @param srcfile2 Second web.xml file to merge. Should be the full path to the file.
     * @param destfile Destination web.xml file to merge into.
     * @param conflictfile Conflict file.
     */
    public MergeWebXml(String srcfile1, String srcfile2, String destfile, String conflictfile) {
        this.srcfile1 = srcfile1;
        this.srcfile2 = srcfile2;
        this.destfile = destfile;
        this.conflictfile = conflictfile;
    }

    /**
     * Run the merge job.
     * 
     * @throws Exception All errors with be thrown to the caller.
     */
    public void runJob() throws Exception {
        RegistryMatcher matcher = new RegistryMatcher();
        matcher.bind(ServletClassType.class, ServletClassTypeTransform.class);
        matcher.bind(JspFileType.class, JspFileTypeTransform.class);
        matcher.bind(UrlPatternType.class, UrlPatternTypeTransform.class);
        matcher.bind(ServletNameType.class, ServletNameTypeTransform.class);
        Serializer serializer = new Persister(matcher, new Format(4, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        WebApp webAppSrc1 = serializer.read(WebApp.class, new InputStreamReader(new FileInputStream(srcfile1), "UTF-8"));
        System.out.println(webAppSrc1);
        WebApp webAppSrc2 = serializer.read(WebApp.class, new InputStreamReader(new FileInputStream(srcfile2), "UTF-8"));
        System.out.println(webAppSrc2);
        WebApp webAppMerge = new WebApp();
        WebApp webAppConflict = new WebApp();
        
        if(webAppSrc1.getDisplayName() == null) {
            webAppMerge.setDisplayName(webAppSrc2.getDisplayName());
        }
        else if(webAppSrc2.getDisplayName() == null) {
            webAppMerge.setDisplayName(webAppSrc1.getDisplayName());
        }
        else {
            webAppConflict.setDisplayName("SM-> " + webAppSrc1.getDisplayName() + " :: TS-> " + webAppSrc2.getDisplayName());
        }

        mergeContextParam(webAppSrc1, webAppSrc2, webAppMerge, webAppConflict);

        mergeFilter(webAppSrc1, webAppSrc2, webAppMerge, webAppConflict);
        
        mergeFilterMapping(webAppSrc1, webAppSrc2, webAppMerge, webAppConflict);
        
        mergeListener(webAppSrc1, webAppSrc2, webAppMerge, webAppConflict);
        
        mergeServlet(webAppSrc1, webAppSrc2, webAppMerge, webAppConflict);
        
        mergeServletMapping(webAppSrc1, webAppSrc2, webAppMerge, webAppConflict);
        
        serializer.write(webAppMerge, new OutputStreamWriter(new FileOutputStream(destfile), "UTF-8"));
        if(conflictfile != null && conflictfile.length() > 0) {
            serializer.write(webAppConflict, new OutputStreamWriter(new FileOutputStream(conflictfile), "UTF-8"));
        }
    }

    private void mergeContextParam(WebApp webAppSrc1, WebApp webAppSrc2, WebApp webAppMerge, WebApp webAppConflict) {
        List<ContextParam> contextParamsMerge = new ArrayList<ContextParam>();
        List<ContextParam> contextParamsConflict = new ArrayList<ContextParam>();
        List<ContextParam> contextParamsSm = webAppSrc1.getContextParams();
        List<ContextParam> contextParamsTs = webAppSrc2.getContextParams();
        if (contextParamsSm == null) {
            contextParamsSm = new ArrayList<ContextParam>();
        }
        if (contextParamsTs == null) {
            contextParamsTs = new ArrayList<ContextParam>();
        }
        compare(contextParamsMerge, contextParamsConflict, contextParamsSm, contextParamsTs);
        if (contextParamsMerge.size() > 0) {
            webAppMerge.setContextParams(contextParamsMerge);
        }
        if (contextParamsConflict.size() > 0) {
            webAppConflict.setContextParams(contextParamsConflict);
        }
    }

    private void mergeFilter(WebApp webAppSrc1, WebApp webAppSrc2, WebApp webAppMerge, WebApp webAppConflict) {
        List<Filter> filterMerge = new ArrayList<Filter>();
        List<Filter> filterConflict = new ArrayList<Filter>();
        List<Filter> filterSm = webAppSrc1.getFilters();
        List<Filter> filterTs = webAppSrc2.getFilters();
        if (filterSm == null) {
            filterSm = new ArrayList<Filter>();
        }
        if (filterTs == null) {
            filterTs = new ArrayList<Filter>();
        }
        compare(filterMerge, filterConflict, filterSm, filterTs);
        if (filterMerge.size() > 0) {
            webAppMerge.setFilters(filterMerge);
        }
        if (filterConflict.size() > 0) {
            webAppConflict.setFilters(filterConflict);
        }
    }

    private void mergeFilterMapping(WebApp webAppSrc1, WebApp webAppSrc2, WebApp webAppMerge, WebApp webAppConflict) {
        List<FilterMapping> filterMappingMerge = new ArrayList<FilterMapping>();
        List<FilterMapping> filterMappingConflict = new ArrayList<FilterMapping>();
        List<FilterMapping> filterMappingSm = webAppSrc1.getFilterMappings();
        List<FilterMapping> filterMappingTs = webAppSrc2.getFilterMappings();
        if (filterMappingSm == null) {
            filterMappingSm = new ArrayList<FilterMapping>();
        }
        if (filterMappingTs == null) {
            filterMappingTs = new ArrayList<FilterMapping>();
        }
        compare(filterMappingMerge, filterMappingConflict, filterMappingSm, filterMappingTs);
        if (filterMappingMerge.size() > 0) {
            webAppMerge.setFilterMappings(filterMappingMerge);
        }
        if (filterMappingConflict.size() > 0) {
            webAppConflict.setFilterMappings(filterMappingConflict);
        }
    }

    private void mergeListener(WebApp webAppSrc1, WebApp webAppSrc2, WebApp webAppMerge, WebApp webAppConflict) {
        List<Listener> listenerMerge = new ArrayList<Listener>();
        List<Listener> listenerConflict = new ArrayList<Listener>();
        List<Listener> listenerSm = webAppSrc1.getListeners();
        List<Listener> listenerTs = webAppSrc2.getListeners();
        if (listenerSm == null) {
            listenerSm = new ArrayList<Listener>();
        }
        if (listenerTs == null) {
            listenerTs = new ArrayList<Listener>();
        }
        compare(listenerMerge, listenerConflict, listenerSm, listenerTs);
        if (listenerMerge.size() > 0) {
            webAppMerge.setListeners(listenerMerge);
        }
        if (listenerConflict.size() > 0) {
            webAppConflict.setListeners(listenerConflict);
        }
    }

    private void mergeServlet(WebApp webAppSrc1, WebApp webAppSrc2, WebApp webAppMerge, WebApp webAppConflict) {
        List<Servlet> servletMerge = new ArrayList<Servlet>();
        List<Servlet> servletConflict = new ArrayList<Servlet>();
        List<Servlet> servletSm = webAppSrc1.getServlets();
        List<Servlet> servletTs = webAppSrc2.getServlets();
        if (servletSm == null) {
            servletSm = new ArrayList<Servlet>();
        }
        if (servletTs == null) {
            servletTs = new ArrayList<Servlet>();
        }
        compare(servletMerge, servletConflict, servletSm, servletTs);
        if (servletMerge.size() > 0) {
            webAppMerge.setServlets(servletMerge);
        }
        if (servletConflict.size() > 0) {
            webAppConflict.setServlets(servletConflict);
        }
    }

    private void mergeServletMapping(WebApp webAppSrc1, WebApp webAppSrc2, WebApp webAppMerge, WebApp webAppConflict) {
        List<ServletMapping> servletMappingMerge = new ArrayList<ServletMapping>();
        List<ServletMapping> servletMappingConflict = new ArrayList<ServletMapping>();
        List<ServletMapping> servletMappingSm = webAppSrc1.getServletMappings();
        List<ServletMapping> servletMappingTs = webAppSrc2.getServletMappings();
        if (servletMappingSm == null) {
            servletMappingSm = new ArrayList<ServletMapping>();
        }
        if (servletMappingTs == null) {
            servletMappingTs = new ArrayList<ServletMapping>();
        }
        compare(servletMappingMerge, servletMappingConflict, servletMappingSm, servletMappingTs);
        if (servletMappingMerge.size() > 0) {
            webAppMerge.setServletMappings(servletMappingMerge);
        }
        if (servletMappingConflict.size() > 0) {
            webAppConflict.setServletMappings(servletMappingConflict);
        }
    }

    /**
     * Iterate source1 list. Check if source2 list contains any object in
     * source1. For each object that is in both lists send object to conflict
     * list. All other objects go to merge list.
     * 
     * @param merge
     * @param conflict
     * @param source1
     * @param source2
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void compare(List merge, List conflict, List source1, List source2) {
        for (Object object : source1) {
            if (source2.contains(object)) {
                conflict.add(object);
                int index = source2.indexOf(object);
                conflict.add(source2.remove(index));
            }
            else {
                merge.add(object);
            }
        }
        for (Object object : source2) {
            merge.add(object);
        }
    }
    
    /**
     * Can run job from command line. Requires 3 or 4 arguments.
     *  1 - source web.xml file 1
     *  2 - source web.xml file 2
     *  3 - destination web.xml file
     *  4 - optional conflict file
     * @param args
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        if(args.length == 3 || args.length == 4) {
            System.out.println("Start: " + new Date());

            String conflict = null;
            if(args.length == 4) {
                conflict = args[3];
            }
            MergeWebXml mergeWebXml = new MergeWebXml(args[0], args[1], args[2], conflict);
            mergeWebXml.runJob();

            System.out.println("  End: " + new Date());
        }
        else {
            System.out.println("Utility to merge 2 web.xml files. Use the following format:");
            System.out.println("  xml.web.MergeWebXml srcfile1 srcfile2 destfile <conflictfile>");
            System.out.println("    srcfile1 - source web.xml file 1");
            System.out.println("    srcfile2 - source web.xml file 2");
            System.out.println("    destfile - destination web.xml file");
            System.out.println("    conflictfile - (optional) conflict web.xml file");
            System.out.println("  All paths should be full O/S paths to the file.");
        }
    }
}
