package com.company;

import jdk.jfr.Unsigned;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Line.FillInstructions();
        ArrayList<Line>lines = new ArrayList<>();
        Line line;
        File inputFile = new File("SICXE.txt");
        File outputFile = new File("output.txt");
        File symbolTableFile = new File("symbTable.txt");
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

    }
}