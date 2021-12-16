package com.company;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Line {
    /* you can make an index for Instruction array instead of a whole instruction object*/
    private static String ProgramName;
    private int indexOFDirective;
    private static int ProgramCounter;
    private static Instruction[] instructions = new Instruction[219];
    private static final String[]  directives = {"START","RESW","RESB","BYTE","WORD","END","BASE"};
    ArrayList<String> line_parts = new ArrayList<>();
    int sizeOfLine;
    Instruction instruction;
   private int location;
   private boolean thereIsInstruction;
   private boolean isAddressNumeric;
   private boolean indirectAddressing;
   private boolean immediateAddressing;
   private boolean labelAtFirst;
   private int objectCode;
   static ArrayList<Label> symbolTable = new ArrayList<>();
    String Address;




    public Line(String line)
    {
        Scanner InputLine = new Scanner(line);
        while (InputLine.hasNext())
        {
            line_parts.add(InputLine.next());
        }
        /* see if we have instruction or directive and see if there is a new label
           sets our instruction member if there is one   */
        checkLine();
        if(!thereIsInstruction)
        {
            directiveCalcSize();
        }
        else if(thereIsInstruction)
        {
            if(instruction.format > 4)
            {
                if(instruction.format == 5)
                {
                    sizeOfLine = 3;
                }
                else
                {
                    sizeOfLine = 4;
                }
            }
            else
            {
                sizeOfLine = instruction.format;
            }
        }
        location = ProgramCounter;
        ProgramCounter+=sizeOfLine;
        if(labelAtFirst)
        {
            symbolTable.add(new Label(line_parts.get(0),location));
        }


    }
    public void buildObjectCodeForLine()
    {
        if(instruction.format==1)
        {
            objectCode= instruction.opcode;
        }
        if(instruction.format==4)
        {
            objectCode = instruction.opcode>>24; // shifting the op code to its place ( most significant 6 bits )
            // n is the 25th bit and i is the 24th
            if(indirectAddressing)
            {
                set_bit(25); // it sets the n bit in the object code implicitly
            }
            else if(immediateAddressing)
            {
                set_bit(24);
            }
            else
            {
                set_bit(25);
                set_bit(24);
            }
            if(Address.contains(",X"))
            {
                set_bit(23); // x bit
                Address = Address.replace(",X","");
            }

            set_bit(20); // set e bit (extension bit)
            if(isAddressNumeric)
            {
                objectCode |= Integer.parseInt(Address); // put the address in its 20 bits
            }
            else
            {
                for(int label = 0;label<symbolTable.size();label++)
                {
                    //search for the label in the symbTable
                    if(symbolTable.get(label).labelName.equals(Address))
                    {
                        objectCode|= symbolTable.get(label).loctr; // put the loctr of the label in its 20 bits
                    }
                }
            }


        }
    }

    public int getLocation() {
        return location;
    }

    public boolean isThereInstruction() {
        return thereIsInstruction;
    }

    public boolean isLabelAtFirst() {
        return labelAtFirst;
    }
    private void directiveCalcSize()
    {
        if(directives[indexOFDirective].equals(directives[0])) // start
        {
           labelAtFirst = false;
           ProgramName = line_parts.get(0);
           ProgramCounter = Integer.parseInt(line_parts.get(line_parts.size()-1)); // first location of the program
            sizeOfLine = 0;
        }
        else if(directives[indexOFDirective].equals(directives[1])) // RESW
        {
            for(int i =0 ;i<line_parts.size();i++)
            {
                if(line_parts.get(i).equals(directives[indexOFDirective]))
                {
                    sizeOfLine = (Integer.parseInt(line_parts.get(i+1))) * 3; //because word is 3 bytes
                }
            }
        }
       else if(directives[indexOFDirective].equals(directives[2])) // RESB
        {
            for(int i =0 ;i<line_parts.size();i++)
            {
                if(line_parts.get(i).equals(directives[indexOFDirective]))
                {
                    sizeOfLine = (Integer.parseInt(line_parts.get(i+1))) ; //because byte is only one byte (obviously)
                }
            }
        }
       else if(directives[indexOFDirective].equals(directives[3])) // BYTE
        {
            for(int i =0 ;i<line_parts.size();i++)
            {
                if(line_parts.get(i).equals(directives[indexOFDirective]))
                {
                    String value = line_parts.get(i+1);
                    if(value.charAt(0)=='C')
                    {
                        value=value.substring(2,value.length()-1);
                        sizeOfLine = value.length();
                    }
                    else if(value.charAt(0)=='X')
                    {
                        value=value.substring(2,value.length()-1);
                        sizeOfLine = ((value.length()))/2;

                    }
                    else if(value.contains(","))
                    {
                        String[] arrOfValues = value.split(",");
                        sizeOfLine = arrOfValues.length;
                    }
                    else
                    {
                        sizeOfLine = 1;
                    }
                }
            }
        }
       else if(directives[indexOFDirective].equals(directives[4])) // WORD
        {
            String value = line_parts.get(line_parts.size()-1);
            if(value.contains(","))
            {
                String[] arrOfValues = value.split(",");
                sizeOfLine = arrOfValues.length * 3 ;
            }
            else
            {
                sizeOfLine = 3;
            }

        }
        else if(directives[indexOFDirective].equals(directives[5])) // END
        {
            sizeOfLine = 0;
        }
        else if(directives[indexOFDirective].equals(directives[6])) // BASE
        {
            sizeOfLine = 0;
            /* another action for pass2 */
        }
    }
    private void checkLine()
    {
        if(line_parts.get(line_parts.size()-1).contains("@"))
        {
            indirectAddressing = true;
            immediateAddressing = false;
             Address = line_parts.get(line_parts.size()-1).replaceFirst("@","");
            try {
                Integer.parseInt(Address);
                isAddressNumeric = true;
            }
            catch(Exception e)
            {
                isAddressNumeric = false;
            }
        }
        else if(line_parts.get(line_parts.size()-1).contains("#"))
        {
            indirectAddressing = false;
            immediateAddressing = true;
             Address = line_parts.get(line_parts.size()-1).replaceFirst("#","");
            try {
                Integer.parseInt(Address);
                isAddressNumeric = true;
            }
            catch(Exception e)
            {
                isAddressNumeric = false;
            }
        }
       for (int i =0 ;i<line_parts.size();i++)
       {
           for (int j = 0; j < 219; j++)
           {
               if (instructions[j].name.equals(line_parts.get(i)))
               {
                   if(i==0)
                   {
                       labelAtFirst = false;
                   }
                   else
                   {
                       labelAtFirst = true;
                   }
                   instruction = new Instruction();
                   instruction.name= instructions[j].name;
                   instruction.opcode= instructions[j].opcode;
                   instruction.format= instructions[j].format;
                   thereIsInstruction = true;
                   return;
               }
           }
       }
       for (int i =0 ;i<line_parts.size();i++)
       {
           for (int j = 0; j < 7; j++)
           {
               if(directives[j].equals(line_parts.get(i)))
               {
                   if(i==0)
                   {
                       labelAtFirst = false;
                   }
                   else
                   {
                       labelAtFirst = true;
                   }
                   indexOFDirective = j;
                   thereIsInstruction = false;

                   return;
               }
           }
       }
    }
    public static void FillInstructions()
    {
        for(int i =0;i<219;i++)
        {
            instructions[i] = new Instruction();
        }
        try
        {
            Scanner file = new Scanner(new File("InstructionSet.txt"));
            Scanner line;

            int i =0;
            while(file.hasNext())
            {
                line = new Scanner(file.nextLine());
                instructions[i].name = line.next();
                instructions[i].format =  line.nextInt();
                instructions[i].opcode = line.nextInt(16);


                for(int j =i+1 ; j <i+3 ; j++)
                {
                    if(j==i+1)
                    {
                        instructions[j].format =  5;
                        instructions[j].name = "&" + instructions[i].name;
                        instructions[j].opcode = instructions[i].opcode;
                    }
                    else if(j==i+2)
                    {
                        instructions[j].format =  6;
                        instructions[j].name = "$" + instructions[i].name;
                        instructions[j].opcode = instructions[i].opcode;
                    }

                }
                if(instructions[i].format==34)
                {
                    instructions[i].format = 3;
                    instructions[i+3].format = 4;
                    instructions[i+3].name = "+" + instructions[i].name;
                    instructions[i+3].opcode = instructions[i].opcode;
                    i++;
                }
                i+=3;
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    private void set_bit(int bitNumber)
    {
         objectCode|=(0b1<<bitNumber);
    }
    private void clr_bit(int bitNumber)
    {
        objectCode&= ~(0b1<<bitNumber);
    }
}
