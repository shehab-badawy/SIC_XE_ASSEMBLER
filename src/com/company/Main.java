package com.company;



import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {


        Line.FillInstructions();
        ArrayList<Line>lines = new ArrayList<>();
        Line line;
        File inputFile = new File("in.txt");
        File outputFile = new File("output.txt");
        File symbolTableFile = new File("symbTable.txt");
        File htmeFile = new File("htmeRecord.txt") ;
        File literalPool = new File ("literalPool.txt");
        // First Pass Creating symbol table
        try
        {
            Scanner input = new Scanner(inputFile);
            PrintWriter output = new PrintWriter(outputFile);
            PrintWriter symb = new PrintWriter(symbolTableFile);
            String objectCodeFormatted;
            while(input.hasNext())
            {
                line = new Line(input.nextLine());
                lines.add(line);
            }

            // Second Pass
            // building object code for each line

            for(int i = 0 ; i < lines.size() ; i++)
            {
                if(lines.get(i).isThereInstruction())
                {
                    lines.get(i).buildObjectCodeForLine();
                }
            }

           //printing the output file

           for(int i=0; i < lines.size(); i++)
           {
               int j_holder=0;
               if(lines.get(i).isLabelAtFirst())
               {

                   symb.printf("%04X      %s\n",lines.get(i).getLocation(),lines.get(i).line_parts.get(0));
               }
               output.printf("%04X ",lines.get(i).getLocation());
               System.out.printf("%04X ",lines.get(i).getLocation());
              for (int j = 0 ; j < lines.get(i).line_parts.size() ; j++)
              {
                  j_holder=j;
                  output.printf("%10s ",lines.get(i).line_parts.get(j));
                  System.out.printf("%s ",lines.get(i).line_parts.get(j));
              }
              if(j_holder==1){
                  output.printf("%10s","");
              }
               if(j_holder==0){
                   output.printf("%10s","");
               }


               if(lines.get(i).isThereInstruction())
               {
                   objectCodeFormatted = String.format("%0"+lines.get(i).sizeOfLine*2+"X",lines.get(i).getObjectCode());
                   output.printf("%15s",objectCodeFormatted);

               }
               else if(lines.get(i).getIndexOFDirective()==3 || lines.get(i).getIndexOFDirective()==4){
                   objectCodeFormatted =lines.get(i).getDirValue();
                   output.printf("%15s",objectCodeFormatted);
               }

               System.out.println();
              output.println();
           }
           output.close();
           symb.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        // writing object code in object code text
        File objectCodeFile = new File("objectcode.txt");
        try
        {
            PrintWriter machineCode = new PrintWriter(objectCodeFile);
           String objectCodeFormatted;
            for(int i = 0 ; i < lines.size() ; i++)
            {
                if(lines.get(i).isThereInstruction())
                {
                    objectCodeFormatted = String.format("%0"+lines.get(i).sizeOfLine*2+"X",lines.get(i).getObjectCode());
                    machineCode.println(objectCodeFormatted);
                }
                else if (lines.get(i).getIndexOFDirective()==3||lines.get(i).getIndexOFDirective()==4){
                    objectCodeFormatted=lines.get(i).getDirValue();
                    machineCode.println(objectCodeFormatted);
                }
                else if(i==lines.size()-1){
                    objectCodeFormatted=lines.get(i).getLtorgObjectCode();
                    machineCode.println(objectCodeFormatted);
                }

            }
            machineCode.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }



        //This part is responsible for the HTE record
        String H_Record = new String("H.");
        String E_Record = new String("E."+String.format("%0"+6+"X",lines.get(1).getLocation()));
        String Name_Changed = new String();
        ArrayList<String>T_Record=new ArrayList<String>();
        ArrayList<String>M_Record=new ArrayList<String>();
        int T_Count=0;
        int T_address=0;
        int T_Size=0;
        int T_index_count=0;

        //Getting the name of the program and concating _ to it if it's less than 6 bits
        Name_Changed= Line.getProgramName();

        for (int i=0;i<6 - (Line.getProgramName().length());i++){
            Name_Changed=Name_Changed.concat("-");
        }

        //Setting the H Record
        H_Record=H_Record+Name_Changed+"."+String.format("%0"+6+"X",lines.get(1).getLocation())+"."+String.format("%0"+6+"X",Line.getProgramCounter());


        //Setting the T Record
        for(int i=0;i<lines.size();i++){
            if(lines.get(i).isThereInstruction()||lines.get(i).getIndexOFDirective()==3||lines.get(i).getIndexOFDirective()==4||i==lines.size()-1||lines.get(i).isLtorgLine()){

                String objectCodeFormatted;
                if(lines.get(i).isThereInstruction()){
                    objectCodeFormatted = String.format("%0"+lines.get(i).sizeOfLine*2+"X",lines.get(i).getObjectCode());
                }
                else if(lines.get(i).getIndexOFDirective()==3||lines.get(i).getIndexOFDirective()==4){
                     objectCodeFormatted=lines.get(i).getDirValue();
                     if(objectCodeFormatted.length()%2!=0){
                         objectCodeFormatted="0"+objectCodeFormatted;
                     }

                }
                else if(lines.get(i).isLtorgLine()){

                    objectCodeFormatted=lines.get(i).getLtorgObjectCode();

                }
                else if(i==lines.size()-1){
                    objectCodeFormatted=lines.get(i).getLtorgObjectCode();
                }
                else {
                    objectCodeFormatted="00";
                }

                if(T_Count==0){
                   T_Record.add(objectCodeFormatted);
                   T_Size+=lines.get(i).sizeOfLine;
                }
                else{
                    T_Record.set(T_index_count,T_Record.get(T_index_count)+"."+objectCodeFormatted);
                    T_Size+=lines.get(i).sizeOfLine;
                }
                T_Count+=lines.get(i).sizeOfLine;
                if(!(i==lines.size()-1)){
                    System.out.println(objectCodeFormatted);
                    if(T_Count+lines.get(i+1).sizeOfLine>30||(i==lines.size()-1)||(!lines.get(i+1).isThereInstruction()&&(lines.get(i+1).getIndexOFDirective()==1||lines.get(i+1).getIndexOFDirective()==2))){
                        T_Record.set(T_index_count,"T."+String.format("%0"+6+"X",T_address)+"."+String.format("%0"+2+"X",T_Size)+"."+T_Record.get(T_index_count));
                        T_address=lines.get(i).getLocation()+lines.get(i).sizeOfLine;
                        T_Size=0;
                        T_Count=0;
                        T_index_count++;
                    }
                }
                else{
                    T_Record.set(T_index_count,"T."+String.format("%0"+6+"X",T_address)+"."+String.format("%0"+2+"X",T_Size)+"."+T_Record.get(T_index_count));
                }


                if(lines.get(i).isThereInstruction() && lines.get(i).sizeOfLine==4){
                    String mLocationFormated = String.format("%0"+6+"X",lines.get(i).getLocation()+1);
                    M_Record.add("M."+mLocationFormated+".05.+"+Name_Changed);
                }
            }
        }


        System.out.println(H_Record);
        for(int i=0;i<T_Record.size();i++){
            System.out.println(T_Record.get(i));
        }
        for(int i=0;i<M_Record.size();i++){
            System.out.println(M_Record.get(i));
        }
        System.out.println(E_Record);




        try{
            PrintWriter htmePrinter = new PrintWriter(htmeFile);
            htmePrinter.println(H_Record);
            for(int i=0;i<T_Record.size();i++){
                htmePrinter.println(T_Record.get(i));
            }
            for(int i=0;i<M_Record.size();i++){
                htmePrinter.println(M_Record.get(i));
            }
            htmePrinter.println(E_Record);
            htmePrinter.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }



        try{
            PrintWriter literalWriter = new PrintWriter(literalPool);
            for (int i=0;i<Line.LiteralPool.size();i++){
                literalWriter.printf("%6s",Line.LiteralPool.get(i).literalName);
                literalWriter.printf("%6s",Line.LiteralPool.get(i).literalLoc);
                literalWriter.println("    "+Line.LiteralPool.get(i).literalvalue);
            }
            literalWriter.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }


}
