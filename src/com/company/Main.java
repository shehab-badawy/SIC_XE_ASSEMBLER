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
        // First Pass Creating symbol table
        try
        {
            Scanner input = new Scanner(inputFile);
            PrintWriter output = new PrintWriter(outputFile);
            PrintWriter symb = new PrintWriter(symbolTableFile);
            while(input.hasNext())
            {
                line = new Line(input.nextLine());
                lines.add(line);
            }
           for(int i=0; i < lines.size(); i++)
           {
               if(lines.get(i).isLabelAtFirst())
               {
                   symb.printf("%04X      %s\n",lines.get(i).getLocation(),lines.get(i).line_parts.get(0));
               }
               output.printf("%04X ",lines.get(i).getLocation());
               System.out.printf("%04X ",lines.get(i).getLocation());
              for (int j = 0 ; j < lines.get(i).line_parts.size() ; j++)
              {
                  output.printf("%s ",lines.get(i).line_parts.get(j));
                  System.out.printf("%s ",lines.get(i).line_parts.get(j));
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
        // Second Pass
        // building object code for each line
        for(int i = 0 ; i < lines.size() ; i++)
        {
            if(lines.get(i).isThereInstruction())
            {
                lines.get(i).buildObjectCodeForLine();
            }
        }
        File objectCodeFile = new File("objectcode.txt");
        // writing object code in object code text
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
                    System.out.println(objectCodeFormatted);
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
            if(lines.get(i).isThereInstruction()){
               String objectCodeFormatted = String.format("%0"+lines.get(i).sizeOfLine*2+"X",lines.get(i).getObjectCode());
                if(T_Count==0){
                   T_Record.add(objectCodeFormatted);
                   T_Size+=lines.get(i).sizeOfLine;
                }
                else{
                    T_Record.set(T_index_count,T_Record.get(T_index_count)+"."+objectCodeFormatted);
                    T_Size+=lines.get(i).sizeOfLine;
                }
                T_Count+=lines.get(i).sizeOfLine;
                if(T_Count+lines.get(i+1).sizeOfLine>=30||(i==lines.size()-2)){
                    T_Record.set(T_index_count,"T."+String.format("%0"+6+"X",T_address)+"."+String.format("%0"+2+"X",T_Size)+"."+T_Record.get(T_index_count));
                    T_address=lines.get(i).getLocation()+lines.get(i).sizeOfLine;
                    T_Size=0;
                    T_Count=0;
                    T_index_count++;
                }
                if(lines.get(i).sizeOfLine==4){
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

    }
}
