/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author andrealottarini
 */
public class PaFacesHtml extends PaFacesElement{

    public PaFacesHtml(){
        super();
    }

    public PaFacesHtml(String id){
        super(id);
    }

    @Override
    public void getCode(Code code) {
        Main.outGen.println("GEN: "+this.id);
        if (this.children.size() > 0){
            code.render.append("\t\toutput.println(\"<"+id+">\");\n");
            for ( PaFacesObject child : this.children ) child.getCode(code);
            code.render.append("\t\toutput.println(\"</"+id+">\");\n");
        }
        else{
            code.render.append("\t\toutput.println(\"<"+id+"/>\");\n");
        }

    }

    @Override
    public String getName() {
        return id;
    }
}
