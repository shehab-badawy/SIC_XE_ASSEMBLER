package com.company;

import java.io.File;
import java.nio.charset.StandardCharsets;
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
   private boolean isAddressLiteral;
   private boolean labelAtFirst;
   private int objectCode;
   static ArrayList<Label> symbolTable = new ArrayList<>();
   private String Address;
   static private String BaseAddress;
   private  String dirValue="";
   static ArrayList<Literal> LiteralPool = new ArrayList<>();
   static ArrayList<Literal> LiteralHolder= new ArrayList<>();
   boolean ltorgLine;
   private String literalValue="";
   private int literalSize;
   private String literalObjectCode="";
   private String ltorgObjectCode="";

    public boolean isLtorgLine() {
        return ltorgLine;
    }

    public String getLtorgObjectCode() {

        return ltorgObjectCode;
    }

    public String getDirValue() {
        return dirValue;
    }

    public static String getProgramName() {
        return ProgramName;
    }

    public static int getProgramCounter() {
        return ProgramCounter;
    }

    public int getIndexOFDirective() {
        return indexOFDirective;
    }

    public int getObjectCode() {
        return objectCode;
    }

    public int getLiteralSize() {
        return literalSize;
    }

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

        if(thereIsInstruction)
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
        else if(ltorgLine||line_parts.get(0).equals("END")){
            LTORG();
        }
        else if(!thereIsInstruction)
        {
            directiveCalcSize();
        }

        location = ProgramCounter;
        ProgramCounter+=sizeOfLine;
        if(labelAtFirst)
        {
            symbolTable.add(new Label(line_parts.get(0),location));
        }
        if(isAddressLiteral){
            boolean checkNewLiteral= true;
            for(int i=0;i<LiteralPool.size();i++) {
                if(literalValue.equals(LiteralPool.get(i).literalvalue)) {
                    checkNewLiteral=false;
                }
            }
            if(checkNewLiteral){

                LiteralPool.add(new Literal(line_parts.get(line_parts.size() - 1), literalValue,literalSize,literalObjectCode));
                LiteralHolder.add(new Literal(line_parts.get(line_parts.size() - 1), literalValue,literalSize,literalObjectCode));
            }

        }


    }
    private  void LTORG(){

        for(int i=0;i<LiteralHolder.size();i++){
            for(int j=0;j<LiteralPool.size();j++){

                if(LiteralPool.get(j).literalvalue.equals(LiteralHolder.get(i).literalvalue)){
                    LiteralPool.get(j).literalLoc=ProgramCounter+sizeOfLine;
                }

            }

            sizeOfLine+=LiteralHolder.get(i).literalSize;

            if(i==LiteralHolder.size()-1){
                ltorgObjectCode +=LiteralHolder.get(i).literalObjectCode;
            }
            else {
                ltorgObjectCode +=LiteralHolder.get(i).literalObjectCode+".";
            }

        }
        while (!LiteralHolder.isEmpty()){
            LiteralHolder.remove(0);

        }
    }
    public void buildObjectCodeForLine()
    {
        if(thereIsInstruction)
        {
            if (instruction.format == 1)
            {
                objectCode = instruction.opcode;
            }
            else if(instruction.format == 2)
            {
                objectCode =objectCode | (instruction.opcode<<8);
                if(line_parts.get(line_parts.size()-1).contains(","))
                {
                    String[] registers = line_parts.get(line_parts.size()-1).split(",");
                    for (int i = 0; i < 2;i++)
                    {
                        if(registers[i].equals("A"))
                        {
                            objectCode = objectCode|(0x0<<4*((i-1)*-1));

                        }
                        else if(registers[i].equals("X"))
                        {
                            objectCode = objectCode|(0x1<<4*((i-1)*-1));
                        }
                        else if(registers[i].equals("L"))
                        {
                            objectCode = objectCode|(0x2<<4*((i-1)*-1));
                        }
                        else if(registers[i].equals("B"))
                        {
                            objectCode = objectCode|(0x3<<4*((i-1)*-1));
                        }
                        else if(registers[i].equals("S"))
                        {
                            objectCode = objectCode|(0x4<<4*((i-1)*-1));
                        }
                        else if(registers[i].equals("T"))
                        {
                            objectCode = objectCode|(0x5<<4*((i-1)*-1));
                        }
                        else if(registers[i].equals("F"))
                        {
                            objectCode = objectCode|(0x6<<4*((i-1)*-1));
                        }
                        else if(registers[i].equals("PC"))
                        {
                            objectCode = objectCode|(0x8<<4*((i-1)*-1));
                        }
                        else if(registers[i].equals("SW"))
                        {
                            objectCode = objectCode|(0x9<<4*((i-1)*-1));
                        }
                    }
                }
                else
                {
                    if(line_parts.get(line_parts.size()-1).equals("A"))
                    {
                        objectCode = objectCode|(0x0<<4);
                    }
                   else if(line_parts.get(line_parts.size()-1).equals("X"))
                    {
                        objectCode = objectCode|(0x1<<4);
                    }
                    else if(line_parts.get(line_parts.size()-1).equals("L"))
                    {
                        objectCode = objectCode|(0x2<<4);
                    }
                    else if(line_parts.get(line_parts.size()-1).equals("B"))
                    {
                        objectCode = objectCode|(0x3<<4);
                    }
                    else if(line_parts.get(line_parts.size()-1).equals("S"))
                    {
                        objectCode = objectCode|(0x4<<4);
                    }
                    else if(line_parts.get(line_parts.size()-1).equals("T"))
                    {
                        objectCode = objectCode|(0x5<<4);
                    }
                    else if(line_parts.get(line_parts.size()-1).equals("F"))
                    {
                        objectCode = objectCode|(0x6<<4);
                    }
                    else if(line_parts.get(line_parts.size()-1).equals("PC"))
                    {
                        objectCode = objectCode|(0x8<<4);
                    }
                    else if(line_parts.get(line_parts.size()-1).equals("SW"))
                    {
                        objectCode = objectCode|(0x9<<4);
                    }
                }
            }
            else if(instruction.format == 3)
            {
                objectCode = instruction.opcode<<16; //moving op code to most 6 significant bits
                if(instruction.name.equals("RSUB"))
                {
                    set_bit(17);
                    set_bit(16);
                    return;
                }
                // n is the 17th bit and i is the 16th
                if (indirectAddressing)
                {
                    set_bit(17); // it sets the n bit in the object code implicitly

                }
                else if (immediateAddressing)
                {
                    set_bit(16);
                }
                else
                {
                    set_bit(17);
                    set_bit(16);
                }
                if (Address.contains(",X"))
                {
                    set_bit(15); // x bit
                    Address = Address.replace(",X", "");
                }
                if (isAddressNumeric)
                {
                    objectCode |= Integer.parseInt(Address); // put the displacement in its 12 bits
                }
                else if(isAddressLiteral){
                    for(int i=0; i<LiteralPool.size();i++){
                        if(Address.equals(LiteralPool.get(i).literalvalue)){

                            if((LiteralPool.get(i).literalLoc - (sizeOfLine+location)) <= 2047 && (LiteralPool.get(i).literalLoc - (sizeOfLine+location)) >=-2048)
                            {

                                set_bit(13);
                                objectCode |= (LiteralPool.get(i).literalLoc - (sizeOfLine+location))&(0xfff);

                            }
                            else
                            {
                                for (int j = 0; j < symbolTable.size(); j++)
                                {

                                    //search for the label in the symbTable
                                    if (symbolTable.get(j).labelName.equals(BaseAddress))
                                    {

                                        set_bit(14);
                                        objectCode |= (LiteralPool.get(i).literalLoc - symbolTable.get(j).loctr )&(0xfff);
                                        break;
                                    }
                                }
                            }

                        }

                    }
                }
                else
                {
                    for (int label = 0; label < symbolTable.size(); label++)
                    {
                        //search for the label in the symbTable
                        if (symbolTable.get(label).labelName.equals(Address))
                        {
                            // condition to see if it is pc relative or base
                            //System.out.printf("label loctr = %X    PC = %X\n", symbolTable.get(label).loctr , sizeOfLine+location);
                            //System.out.printf("disp = %X    \n", symbolTable.get(label).loctr - (sizeOfLine+location));
                            if((symbolTable.get(label).loctr - (sizeOfLine+location)) <= 2047 && (symbolTable.get(label).loctr - (sizeOfLine+location)) >=-2048)
                            {
                                set_bit(13);
                                objectCode |= (symbolTable.get(label).loctr - (sizeOfLine+location))&(0xfff);
                            }
                            else
                            {
                                for (int i = 0; i < symbolTable.size(); i++)
                                {
                                    //search for the label in the symbTable
                                    if (symbolTable.get(i).labelName.equals(BaseAddress))
                                    {

                                        set_bit(14);
                                        objectCode |= (symbolTable.get(label).loctr - symbolTable.get(i).loctr )&(0xfff);
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
            else if (instruction.format == 4)
            {
                objectCode = instruction.opcode;
                objectCode = objectCode<<24; // shifting the op code to its place ( most significant 6 bits )
                set_bit(20); // set e bit (extension bit)
                if(instruction.name.equals("+RSUB"))
                {
                    set_bit(25);
                    set_bit(24);
                    return;
                }
                // n is the 25th bit and i is the 24th
                if (indirectAddressing)
                {
                    set_bit(25); // it sets the n bit in the object code implicitly

                }
                else if (immediateAddressing)
                {
                    set_bit(24);
                }
                else
                {
                    set_bit(25);
                    set_bit(24);
                }
                if (Address.contains(",X"))
                {
                    set_bit(23); // x bit
                    Address = Address.replace(",X", "");
                }
                if (isAddressNumeric)
                {
                    objectCode |= Integer.parseInt(Address); // put the address in its 20 bits
                }
                else if (isAddressLiteral){
                    for (int i = 0; i < LiteralPool.size(); i++) {
                        //search for the label in the symbTable

                        if (Address.equals(LiteralPool.get(i).literalvalue)) {
                            objectCode |= LiteralPool.get(i).literalLoc; // put the location of the literal in its 20 bits
                            break;
                        }
                    }
                }
                else
                {
                    for (int label = 0; label < symbolTable.size(); label++)
                    {
                        //search for the label in the symbTable
                        if (symbolTable.get(label).labelName.equals(Address))
                        {
                            objectCode |= symbolTable.get(label).loctr; // put the loctr of the label in its 20 bits
                            break;
                        }
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
                        byte [] convert=value.getBytes(StandardCharsets.US_ASCII);
                        int z;
                        String l= new String();
                        for(int j=0;j<convert.length;j++){
                            l+=convert[j];
                            System.out.println(convert[j]);
                        }
                        for(int x=0;x<l.length();x+=2){
                            z=Integer.parseInt(String.valueOf(l.charAt(x))+String.valueOf(l.charAt(x+1)));
                            dirValue+=String.format("%X", z);
                        }


                    }
                    else if(value.charAt(0)=='X')
                    {
                        value=value.substring(2,value.length()-1);
                        if (value.length()%2!=0){
                           sizeOfLine = (value.length())/2+1;
                        }
                        else {
                            sizeOfLine = ((value.length()))/2;
                        }


                        dirValue=value;
                    }
                    else if(value.contains(","))
                    {
                        String[] arrOfValues = value.split(",");
                        for (int j=0;j<arrOfValues.length;j++){
                            dirValue+=arrOfValues[j];
                        }
                        sizeOfLine = arrOfValues.length;

                    }
                    else
                    {

                        int z=Integer.parseInt(value);
                        dirValue=String.format("%X",z);
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
                int temp;
                for (int j=0;j<arrOfValues.length;j++){
                    temp=Integer.parseInt(arrOfValues[j]);
                    dirValue+=String.format("%0"+6+"X",temp);
                }
                sizeOfLine = arrOfValues.length * 3 ;


            }
            else
            {
                sizeOfLine = 3;
                int valueHolder=Integer.parseInt(value);
                dirValue = String.format("%0"+6+"X",valueHolder);

            }

        }
        else if(directives[indexOFDirective].equals(directives[5])) // END
        {
            sizeOfLine = 0;
        }
        else if(directives[indexOFDirective].equals(directives[6])) // BASE
        {
            sizeOfLine = 0;
            BaseAddress = line_parts.get(line_parts.size()-1);
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
        {   indirectAddressing = false;
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
        else if (line_parts.get(line_parts.size()-1).contains("=")){
            isAddressLiteral = true;
            indirectAddressing = false;
            immediateAddressing = false;
            literalValue = line_parts.get(line_parts.size()-1);
            Address = literalValue;
            if(literalValue.charAt(1)=='C')
            {
                String value=literalValue.substring(3,literalValue.length()-1);
                literalSize = value.length();
                byte [] convert=value.getBytes(StandardCharsets.US_ASCII);
                int z;
                String l= new String();
                for(int j=0;j<convert.length;j++){
                    l+=convert[j];

                }
                for(int x=0;x<l.length();x+=2){
                    z=Integer.parseInt(String.valueOf(l.charAt(x))+String.valueOf(l.charAt(x+1)));
                    literalObjectCode+=String.format("%X", z);
                }


            }
            else if(literalValue.charAt(1)=='X')
            {
                String value=literalValue.substring(3,literalValue.length()-1);
                if (value.length()%2!=0){
                    literalSize = ((value.length()))/2+1;
                }
                else {
                    literalSize = ((value.length()))/2;
                }
                if(value.length()%2!=0){
                    literalObjectCode="0"+value;
                }
                else{
                    literalObjectCode=value;
                }

            }

            else
            {

                int value=Integer.parseInt(literalValue.substring(1,literalValue.length()-1));
                literalObjectCode=String.format("%0"+6+"X",value);
                literalSize = 3;
            }

            try {
                Integer.parseInt(Address);
                isAddressNumeric = true;
            }
            catch(Exception e)
            {
                isAddressNumeric = false;
            }
        }
        else
        {

            Address = line_parts.get(line_parts.size()-1);
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
       if(line_parts.get(0).equals("LTORG")||line_parts.get(0).equals("ltorg")){
           thereIsInstruction=false;
           ltorgLine=true;
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
