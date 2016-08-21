//This file is part of JPACS.
//
//    JPACS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    JPACS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with JPACS.  If not, see <http://www.gnu.org/licenses/>.

package br.pucminas.dcc.jpacs.core;

import br.pucminas.dcc.jpacs.Internacionalizacao;
import java.util.Vector;

/**
 * Classe do Processador virtual do JPACS
 */
public class Processor {
    //contador de programa
    protected static int PC = 0;
    
    //registradores
    protected static int Regs[] = new int[27];
    protected static int RegTmp;
    
     /* Lista dos Registradores
    Posiçao do Vetor - Nome do Registrador
             1			A
             2			B
             3			C
             4			D
             5			E
             6			F
             7			G
             8			H
             .			.
             .			.
             .			.
      
             Z			26
      
         RegTmp serve na hora da compilação de código para apontar a posição
         de memória onde serão escritas as instruções
      */
    
    //Flags
    protected static boolean FlagZero;
    protected static boolean FlagCarry;
    protected static boolean FlagSinal;
    
    //Memoria
    protected static ClasseMemoria Memoria = new ClasseMemoria();
    
    //Outras Variaveis
    protected static String EstXML;
    int flagErrorProcessador = 0;
    int flagErrorMemoria = 0;
    //int countAux = 0;
    
    //Sistema de Tolerancia a Falhas
    public int Controlador(int PC) { 
        int count = 0;
        String binario;
        String paridade;
        String b;
        
        do{
        binario = Integer.toBinaryString(PC); //Transformando PC em Binario
        paridade = adicionarParidade(binario);//Adicionando Paridade ao PC
        
        // Sistema que cria uma falha para teste
        
//        if(countAux == 0) {
//            paridade += 1;
//            countAux++;
//        }
        b = Memoria.Controlador(paridade);
        flagErrorProcessador++;
        } while(b.equals("-1") && flagErrorProcessador < 2); //Erro
        
        if(flagErrorProcessador > 2) {
            System.out.println("Error PARIDADE PROCESSADOR N ESTA CORRETA");
            System.exit(0);
            //Egostado Numero de Tentativas Maximas
        }
        
        //Tratamento da Memoria
        for(int i = 0; i < b.length()-1; i++) {
			if(b.charAt(i) == '1')
				count++;	
		}
		if(count % 2 == 0 && b.endsWith("1")) {
			System.out.println("funcionou paridade memoria");
			flagErrorMemoria = 0;
                        flagErrorProcessador = 0;
			return Integer.parseInt(b.substring(0,b.length()-1), 2); //Paridade OK!
		}
		if(count % 2 != 0 && b.endsWith("0")) {
			System.out.println("funcionou paridade memoria");
			flagErrorMemoria = 0;
                        flagErrorProcessador = 0;
			return Integer.parseInt(b.substring(0,b.length()-1), 2); //Paridade OK!
		}
		if(flagErrorMemoria < 2) {
			flagErrorMemoria++;
                        return Controlador(PC); //Refaz o envio de mensagem.
                        
                }
                
	System.out.println("Error PARIDADE MEMORIA N ESTA CORRETA");		
	System.exit(0); //Egostado Numero de Tentativas Maximas
        return -1;
    }
    
    public String adicionarParidade(String b) {
		int count = 0;
		for(int i = 0; i < b.length(); i++) {
			if(b.charAt(i) == '1')
				count++;	
		}
		if(count % 2 == 0)
			return b += 1;
		return b += 0;
    }
    
    
    
    
    //Métodos
    public boolean[] PegaFlags() {
        boolean[] Ret=new boolean[3];
        Ret[0]=FlagZero;
        Ret[1]=FlagCarry;
        Ret[2]=FlagSinal;
        
        return Ret;
    }
    
    public int PegaPC() {
        return PC;
    }
    
    public int Pilha() {
        return Memoria.Ram.Topo;
    }
    
    public int[] PegaRegs() {
        int[] Ret=new int[27];
        int i;
        
        for(i=0; i<27; i++)
            Ret[i]=Regs[i];
        
        return Ret;
    }
    
    public void LimpaProcessador() {
        //limpa o processador
        int i;
        
        NovaMemoria(); //nova memoria
        for(i=0; i<Regs.length; i++)
            Regs[i]=0; //limpa os registradores
        
        FlagZero=FlagCarry=FlagSinal=false; //limpa os flags
        
        PC=0; //zera o pc
    }
    
    public void NovaMemoria() {
        Memoria.Refaz();
    }
    
    public java.util.Vector Acessando(String Com) {
        String Tipo=SeparaDoComeco(Com);
        Com=SeparaDoFinal(Com);
        
        try{
            
            if(Tipo.charAt(0)=='e') //acessar posição para escrita
                Memoria.Gravar(Integer.parseInt(Com), 0);
            else //acessar posicao para leitura
                Memoria.Acessar(Integer.parseInt(Com));
        }catch(Exception e){
            System.out.println(e);
        }
        
        return Memoria.PegaOutput();
    }
    
    public String Calcula()//retorna o Hit Ratio, Tempo Medio de Acesso e Tempo Total em formato de String
    {
        String Ret;
        Memoria.Calcula();
        
        float Ganho=(1-(Memoria.TempoTotal) / (float)((Memoria.AcessosCache)*Memoria.TempoAcessoMemPrincipal))*100;
        Ganho=((int)(Ganho*100))/100.0f;
        
        Ret="\n"+Internacionalizacao.get("Memory.Title")
        +"\n   "+Internacionalizacao.get("Report.Access")+": "+Memoria.AcessosMemPrincipal
                +"\n   "+Internacionalizacao.get("Config.Memory.Time")+": "+Memoria.TempoAcessoMemPrincipal+"ns"
                +"\n   "+Internacionalizacao.get("Report.Total.Time")+": "+Memoria.MemPrincipalTempoTotal+"ns"
                +"\n "
                +"\n"+Internacionalizacao.get("Cache.Title")
                +"\n   "+Internacionalizacao.get("Report.Access")+": "+Memoria.AcessosCache
                +"\n   "+Internacionalizacao.get("Config.Cache.Time")+": "+Memoria.TempoAcessoCache+"ns"
                +"\n   "+Internacionalizacao.get("Report.Total.Time")+": "+Memoria.CacheTempoTotal+"ns"
                +"\n   "+Internacionalizacao.get("Report.Hit.Ratio")+": "+Memoria.HitRatio+"%"
                +"\n   "+Internacionalizacao.get("Report.Miss.Ratio")+": "+(100-Memoria.HitRatio)+"%"
                +"\n "
                +"\n"+Internacionalizacao.get("Report.Access.Total.Time")
                +"\n   "+Internacionalizacao.get("Report.Access.Total.Cache")+": "+Memoria.TempoTotal+"ns"
                +"\n   "+Internacionalizacao.get("Report.Access.Total.No.Cache")+": "+(Memoria.AcessosCache*Memoria.TempoAcessoMemPrincipal)+"ns"
                +"\n   "+Internacionalizacao.get("Report.Gain")+": "+Ganho+"%";
        
        EstXML="\n\t\t<estastisticas>"
                +"\n\t\t\t<memprincipal acessos=\""+Memoria.AcessosMemPrincipal+"\" "
                +"tempoacesso=\""+Memoria.TempoAcessoMemPrincipal+"\" "
                +"tempototal=\""+Memoria.MemPrincipalTempoTotal+"\" />"
                +"\n\t\t\t<memcache acessos=\""+Memoria.AcessosCache+"\" "
                +"tempoacesso=\""+Memoria.TempoAcessoCache+"\" "
                +"tempototal=\""+Memoria.CacheTempoTotal+"\" "
                +"taxaacerto=\""+Memoria.HitRatio+"\" "
                +"taxaerro=\""+(100-Memoria.HitRatio)+"\" />"
                +"\n\t\t\t<total "
                +"comcache=\""+Memoria.TempoTotal+"\" "
                +"semcache=\""+((Memoria.AcessosCache)*Memoria.TempoAcessoMemPrincipal)+"\" "
                +"ganho=\""+Ganho/100+"\" />"
                +"\n\t\t</estastisticas>";
        
        return Ret;
    }
    
    public String EstatisticasXml() {
        return EstXML;
    }
    
    public void ZeraCalculos() {
        Memoria.Cache.Hits=0;
        Memoria.Cache.Misses=0;
        Memoria.Cache.TempoTotal=0;
        Memoria.Cache.NAcesso=0;
    }
    
    public int[] Compilador(String Programa) throws Exception {
        int i=0, k;
        String Linhas[];
        Linhas=SeparaEmLinhas(Programa);
        int LinhaErros[]=new int[Linhas.length];
        RegTmp=0;
        
        try{
            
            for(i=0, k=0; i<Linhas.length; i++) {
                LinhaErros[i]=-1;
                if(Linhas[i].length()>1)
                    if(!Compila(Linhas[i])) //houve erros durante a compilação
                        LinhaErros[k++]=i+1; //pega a linha onde ocorreu o erro
            }
            
        }catch(Exception e){
            throw new JPACSCompilerException("Exceção ocorrida na linha "+(i+1)+" do código.");
        }
        
        return LinhaErros;
    }
    
    protected boolean Compila(String Comando) throws Exception//traduz comando para linguagem da máquina
    {
        String Aux=SeparaDoFinal(Comando);
        Comando=SeparaDoComeco(Comando);
        boolean Retorno=true; //True = Não houve erros de compilação, False = Algum comando não reconhecido
        
        try{
            
            if(Aux!=null)
                if(Aux.compareToIgnoreCase("Main")==0)//compara
                {
                //main é na verdade a posição inicial do PC
                Comando=SeparaDoFinal(Comando);//separa o end. no caso de usuarios descuidados
                RegTmp=Integer.parseInt(Comando);
                PC=RegTmp;
                } else if(Aux.compareToIgnoreCase("Prog")==0) {
                //seg indica a posicao de um programa na memória mas não
                //necessariamente o principal
                Comando=SeparaDoFinal(Comando);//separa o end. no caso de usuarios descuidados
                RegTmp=Integer.parseInt(Comando);//end do prog
                } else if(Aux.compareToIgnoreCase("Mov")==0) {
                //Copia conteúdo do reg2 para reg1
                Aux=SeparaDoFinal(Comando);//pega o primeiro Reg
                Comando=SeparaDoComeco(Comando);
                Memoria.GravarDireto(RegTmp++, TosMov.Codigo);//Código do Mov
                Memoria.GravarDireto(RegTmp++, StrParaReg(Aux));//grava reg1
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));//grava reg2
                } else if(Aux.compareToIgnoreCase("Mvi")==0) {
                //Copia dado para reg1
                Aux=SeparaDoFinal(Comando);//pega o primeiro Reg
                Comando=SeparaDoComeco(Comando);
                Memoria.GravarDireto(RegTmp++, TosMvi.Codigo);//Código do Mvi
                Memoria.GravarDireto(RegTmp++, StrParaReg(Aux));//grava reg1
                Memoria.GravarDireto(RegTmp++, Integer.parseInt(Comando));//grava dado
                } else if(Aux.compareToIgnoreCase("Inc")==0) {
                //Incrementa reg
                Memoria.GravarDireto(RegTmp++, TosInc.Codigo);//Código do Inc
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));//grava reg
                } else if(Aux.compareToIgnoreCase("Dec")==0) {
                //Decrementa reg1
                Memoria.GravarDireto(RegTmp++, TosDec.Codigo);//Código do Dec
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));//grava reg
                } else if(Aux.compareToIgnoreCase("Add")==0) {
                //Soma dois regs
                Aux=SeparaDoFinal(Comando);//pega o primeiro Reg
                Comando=SeparaDoComeco(Comando);
                Memoria.GravarDireto(RegTmp++, TosAdd.Codigo);//Código do Add
                Memoria.GravarDireto(RegTmp++, StrParaReg(Aux));//grava reg1
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));//grava reg2
                } else if(Aux.compareToIgnoreCase("Sub")==0) {
                //Subtrai dois regs
                Aux=SeparaDoFinal(Comando);//pega o primeiro Reg
                Comando=SeparaDoComeco(Comando);
                Memoria.GravarDireto(RegTmp++, TosSub.Codigo);//Código do Sub
                Memoria.GravarDireto(RegTmp++, StrParaReg(Aux));//grava reg1
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));//grava reg2
                } else if(Aux.compareToIgnoreCase("Or")==0) {
                //Or l�gico entre dois regs
                Aux=SeparaDoFinal(Comando);//pega o primeiro Reg
                Comando=SeparaDoComeco(Comando);
                Memoria.GravarDireto(RegTmp++, TosOr.Codigo);//Código do Or
                Memoria.GravarDireto(RegTmp++, StrParaReg(Aux));//grava reg1
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));//grava reg2
                } else if(Aux.compareToIgnoreCase("And")==0) {
                //And l�gico entre dois regs
                Aux=SeparaDoFinal(Comando);//pega o primeiro Reg
                Comando=SeparaDoComeco(Comando);
                Comando=SeparaDoFinal(Comando);//pega o segundo reg
                Memoria.GravarDireto(RegTmp++, TosAnd.Codigo);//Código do And
                Memoria.GravarDireto(RegTmp++, StrParaReg(Aux));//grava reg1
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));//grava reg2
                } else if(Aux.compareToIgnoreCase("Cmp")==0) {
                //Compara dois regs
                Aux=SeparaDoFinal(Comando);//pega o primeiro Reg
                Comando=SeparaDoComeco(Comando);
                Comando=SeparaDoFinal(Comando);//pega o segundo reg
                Memoria.GravarDireto(RegTmp++, TosCmp.Codigo);//Código do Cmp
                Memoria.GravarDireto(RegTmp++, StrParaReg(Aux));//grava reg1
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));//grava reg2
                } else if(Aux.compareToIgnoreCase("Ldi")==0) {
                //Carrega o reg com o conteúdo da posição de memoria
                Aux=SeparaDoFinal(Comando);//pega o Reg
                Comando=SeparaDoComeco(Comando);
                Comando=SeparaDoFinal(Comando);//pega o Endereco
                Memoria.GravarDireto(RegTmp++, TosLdi.Codigo);//Código do Load
                Memoria.GravarDireto(RegTmp++, StrParaReg(Aux));//Reg
                Memoria.GravarDireto(RegTmp++, Integer.parseInt(Comando));//end
                } else if(Aux.compareToIgnoreCase("Sti")==0) {
                //Carrega o conteúdo da posição de memoria com o valor do reg
                Aux=SeparaDoFinal(Comando);//pega o Reg
                Comando=SeparaDoComeco(Comando);
                Comando=SeparaDoFinal(Comando);//pega o Endereco
                Memoria.GravarDireto(RegTmp++, TosSti.Codigo);//Código do Store
                Memoria.GravarDireto(RegTmp++, StrParaReg(Aux));//reg
                Memoria.GravarDireto(RegTmp++, Integer.parseInt(Comando));//end
                } else if(Aux.compareToIgnoreCase("Ldr")==0) {
                //Carrega o reg com o conteúdo da posição de memoria usando acesso indireto
                Aux=SeparaDoFinal(Comando);//pega o Reg1
                Comando=SeparaDoComeco(Comando);
                Comando=SeparaDoFinal(Comando);//pega o Reg2 (Endereco)
                Memoria.GravarDireto(RegTmp++, TosLdr.Codigo);//Código do Load
                Memoria.GravarDireto(RegTmp++, StrParaReg(Aux));//reg1
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));// reg2 (end)
                } else if(Aux.compareToIgnoreCase("Str")==0) {
                //Carrega o conteúdo da posição de memoria com o valor do reg usando acesso indireto
                Aux=SeparaDoFinal(Comando);//pega o Reg
                Comando=SeparaDoComeco(Comando);
                Comando=SeparaDoFinal(Comando);//pega o Reg2 (Endereco)
                Memoria.GravarDireto(RegTmp++, TosStr.Codigo);//Código do Store
                Memoria.GravarDireto(RegTmp++, StrParaReg(Aux));//reg1
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));//reg2 (end)
                } else if(Aux.compareToIgnoreCase("Jmp")==0) {
                //Jumper incondicional
                Comando=SeparaDoFinal(Comando);//pega o Endereco
                Memoria.GravarDireto(RegTmp++, TosJump.Codigo);//Código do jump
                Memoria.GravarDireto(RegTmp++, TosJump.Padrao);//codigo de jump incondicional
                Memoria.GravarDireto(RegTmp++, Integer.parseInt(Comando));//end
                } else if(Aux.compareToIgnoreCase("Jnz")==0) {
                //Jumper se o Flag de Zero = false
                Comando=SeparaDoFinal(Comando);//pega o Endereco
                Memoria.GravarDireto(RegTmp++, TosJump.Codigo);//Código do jump
                Memoria.GravarDireto(RegTmp++, TosJump.NaoZero);//codigo de jnz
                Memoria.GravarDireto(RegTmp++, Integer.parseInt(Comando));//end
                } else if(Aux.compareToIgnoreCase("Jz")==0) {
                //Jumper se o Flag de Zero = true
                Comando=SeparaDoFinal(Comando);//pega o Endereco
                Memoria.GravarDireto(RegTmp++, TosJump.Codigo);//Código do jump
                Memoria.GravarDireto(RegTmp++, TosJump.Zero);//codigo de jz
                Memoria.GravarDireto(RegTmp++, Integer.parseInt(Comando));//end
                } else if(Aux.compareToIgnoreCase("Call")==0) {
                //Chama uma fun��o (Jump q guarda endereço de retorno)
                Comando=SeparaDoFinal(Comando);//pega o Endereco
                Memoria.GravarDireto(RegTmp++, TosCall.Codigo);//Código do Call
                Memoria.GravarDireto(RegTmp++, Integer.parseInt(Comando));//end
                } else if(Aux.compareToIgnoreCase("Ret")==0) {
                //Retorna ao �ltimo call (na verdade ultimo elemento da pilha)
                Memoria.GravarDireto(RegTmp++, TosRet.Codigo);//Código do Ret
                } else if(Aux.compareToIgnoreCase("Push")==0) {
                //Guarda reg na pilha
                Comando=SeparaDoFinal(Comando);//pega o reg
                Memoria.GravarDireto(RegTmp++, TosPush.Codigo);//Código do Push
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));//grava reg
                } else if(Aux.compareToIgnoreCase("Pop")==0) {
                //Carrega reg com ultimo elemento da pilha
                Comando=SeparaDoFinal(Comando);//pega o reg
                Memoria.GravarDireto(RegTmp++, TosPop.Codigo);//Código do Pop
                Memoria.GravarDireto(RegTmp++, StrParaReg(Comando));//grava reg
                } else if(Aux.compareToIgnoreCase("Halt")==0) {
                //Termina a execu��o do programa e para o processador
                Memoria.GravarDireto(RegTmp++, TosHalt.Codigo);//Código do Halt
                } else if(Aux.compareToIgnoreCase("Nop")==0 ) {
                Memoria.GravarDireto(RegTmp++, TosNop.Codigo);//Código do Nop
                } else {//qualquer outro comando será interpretado como Nop mas uma msg de erro será exibida
                
                Memoria.GravarDireto(RegTmp++, TosNop.Codigo);//Código do Nop
                Retorno=false;
                }
        }catch(Exception e){
            Retorno=false;
            throw new Exception(e);
        }finally {
            return Retorno; //true = sem erros
        }
    }
    
    public String SeparaDoFinal(String Comando) {
        int i=0, j;
        String Ret=Comando;
        if(Ret!=null)
            while(i<Ret.length() && Ret.charAt(i)!=' ' && Ret.charAt(i)!=',')
                i++;
        
        if(i>0 && i<Ret.length())
            Ret=Ret.substring(0,i);
        return Ret.trim();
    }
    
    public String SeparaDoComeco(String Comando) {
        int i=0;
        String Ret=Comando;
        if(Ret!=null)
            while(i<Ret.length() && Ret.charAt(i)!=' ' && Ret.charAt(i)!=',')
                i++;
        
        if(i>0 && i<Ret.length())
            Ret=Ret.substring(i+1);
        return Ret.trim();
    }
    
    private int StrParaReg(String C) {
        int R;
        switch(C.charAt(0)) {
            case 'B':
            case 'b': R=2;
            break;
            case 'C':
            case 'c': R=3;
            break;
            case 'D':
            case 'd': R=4;
            break;
            case 'E':
            case 'e': R=5;
            break;
            case 'F':
            case 'f': R=6;
            break;
            case 'G':
            case 'g': R=7;
            break;
            case 'H':
            case 'h': R=8;
            break;
            case 'I':
            case 'i': R=9;
            break;
            case 'J':
            case 'j': R=10;
            break;
            case 'K':
            case 'k': R=11;
            break;
            case 'L':
            case 'l': R=12;
            break;
            case 'M':
            case 'm': R=13;
            break;
            case 'N':
            case 'n': R=14;
            break;
            case 'O':
            case 'o': R=15;
            break;
            case 'P':
            case 'p': R=16;
            break;
            case 'Q':
            case 'q': R=17;
            break;
            case 'R':
            case 'r': R=18;
            break;
            case 'S':
            case 's': R=19;
            break;
            case 'T':
            case 't': R=20;
            break;
            case 'U':
            case 'u': R=21;
            break;
            case 'V':
            case 'v': R=22;
            break;
            case 'W':
            case 'w': R=23;
            break;
            case 'X':
            case 'x': R=24;
            break;
            case 'Y':
            case 'y': R=25;
            break;
            case 'Z':
            case 'z': R=26;
            break;
            
            default: R=1; //A
            break;
        }
        return R;
    }
    
    public static String[] SeparaEmLinhas(String Texto) {
        String Temp[] = new String[100];
        int i, j, k;
        char C;
        
        for(i=0, j=0, k=0; i<Texto.length(); i++)
            if(Texto.charAt(i)=='\n') {
            Temp[k]=Texto.substring(j,i);
            j=i+1;
            k++;
            }
        
        Temp[k]=Texto.substring(j);
        k++;
        
        String Ret[] = new String[k];
        for(i=0; i<k; i++)
            Ret[i]=Temp[i];
        return Ret;
    }
    
    public Vector VisualizaRam() {
        return Memoria.VisualizaRam();
    }
    
    public CacheSlot[] VisualizaCache() {
        return Memoria.Cache.Visualiza();
    }
    
    public java.util.Vector Processando() throws Exception {
        if(PC>=0) {
            Processa();
            return Memoria.PegaOutput();
        } else
            return null;
    }
    
    protected void Processa() {
        int Codigo;
        try{
            Assembly Instrucao=new TosHalt();
            Codigo=Controlador(PC);
            PC++;
            
            switch(Codigo) {
                case TosMov.Codigo:
                    Instrucao=new TosMov();
                    break;
                case TosMvi.Codigo:
                    Instrucao=new TosMvi();
                    break;
                case TosInc.Codigo:
                    Instrucao=new TosInc();
                    break;
                case TosDec.Codigo:
                    Instrucao=new TosDec();
                    break;
                case TosAdd.Codigo:
                    Instrucao=new TosAdd();
                    break;
                case TosSub.Codigo:
                    Instrucao=new TosSub();
                    break;
                case TosOr.Codigo:
                    Instrucao=new TosOr();
                    break;
                case TosAnd.Codigo:
                    Instrucao=new TosAnd();
                    break;
                case TosCmp.Codigo:
                    Instrucao=new TosCmp();
                    break;
                case TosLdi.Codigo:
                    Instrucao=new TosLdi();
                    break;
                case TosSti.Codigo:
                    Instrucao=new TosSti();
                    break;
                case TosLdr.Codigo:
                    Instrucao=new TosLdr();
                    break;
                case TosStr.Codigo:
                    Instrucao=new TosStr();
                    break;
                case TosJump.Codigo:
                    Instrucao=new TosJump();
                    break;
                case TosCall.Codigo:
                    Instrucao=new TosCall();
                    break;
                case TosRet.Codigo:
                    Instrucao=new TosRet();
                    break;
                case TosPush.Codigo:
                    Instrucao=new TosPush();
                    break;
                case TosPop.Codigo:
                    Instrucao=new TosPop();
                    break;
                case TosNop.Codigo:
                    Instrucao=new TosNop();
                    break;
                default:
                    Instrucao=new TosHalt();
                    break;
            }
            
            Instrucao.Executa();
            
        }catch(Exception e){
            System.out.println("Erro: Processor.Processando()->"+e);
        }
        
    }
    
    public boolean Finis() {
        return (PC<0);
    }
};

/**
 * Classe abstrata do assembly baseado no assembly do 8085, 
 * estende o processador para ter acesso a tudo
 * do processador: memória, registradores, flags, pc.
 */
abstract class Assembly extends Processor {
    /* Lista de Comandos *
     
         1 - Mov Reg, Reg
         2 - Mvi Reg, Dado
         3 - Inc Reg
         4 - Dec Reg
         5 - Add Reg, Reg
         6 - Sub Reg, Reg
         7 - Or Reg, Reg
         8 - And Reg, Reg
         9 - Cmp Reg, Reg
        10 - Ldi Reg, Endereco
        11 - Sti Reg, Endereco
        12 - Jmp Endereco
        13 - Nop
        14 - Call Endereco
        15 - Ret
        16 - Push Reg
        17 - Pop Reg
        18 - Halt
        19 - Ldr Reg, Endereco
        20 - Str Reg, Endereco
     */
    
    abstract public void Executa();
    
    //Métodos
    public void AtualizaFlagZero(int Item) {
        if(Item!=0)
            FlagZero=false;
        else
            FlagZero=true;
    }
    
    public void AtualizaFlagSinal(int Item) {
        if(Item>0)
            FlagSinal=false;
        else
            FlagSinal=true;
    }
}

class TosMov extends Assembly {
    //carrega reg com reg
    //Mov A,B
    public static final int Codigo = 1;
    
    public void Executa() {
        int Reg1=Controlador(PC);
        PC++;
        int Reg2=Controlador(PC);
        PC++;
        
        Regs[Reg1]=Regs[Reg2];
    }
};

class TosMvi extends Assembly {
    //carrega reg com dado
    //Mvi A,40
    public static final int Codigo = 2;
    
    public void Executa() {
        int Reg=Controlador(PC);
        PC++;
        int Dado=Controlador(PC);
        PC++;
        
        Regs[Reg]=Dado;
    }
};

class TosInc extends Assembly {
    //incrementa reg
    //Inc A
    public static final int Codigo = 3;
    
    public void Executa() {
        int Reg=Controlador(PC);
        PC++;
        
        if(Reg<0) Reg=0;
        else if(Reg>8) Reg=8;
        
        Regs[Reg]++;
        AtualizaFlagZero(Regs[Reg]);//atualiza flag de zero
        AtualizaFlagSinal(Regs[Reg]);//atualiza flag de sinal
    }
    
};

class TosDec extends Assembly {
    //Decrementa reg
    //Dec A
    public static final int Codigo = 4;
    
    public void Executa() {
        int Reg=Controlador(PC);
        PC++;
        
        Regs[Reg]--;
        AtualizaFlagZero(Regs[Reg]);//atualiza flag de zero
        AtualizaFlagSinal(Regs[Reg]);//atualiza flag de sinal
    }
    
};

class TosAdd extends Assembly {
    //Soma reg com reg (resultado no primeiro reg)
    //Add A,B
    public static final int Codigo = 5;
    
    public void Executa() {
        int Reg1=Controlador(PC);
        PC++;
        int Reg2=Controlador(PC);
        PC++;
        
        Regs[Reg1]+=Regs[Reg2];
        
        AtualizaFlagZero(Regs[Reg1]);//atualiza flag de zero
        AtualizaFlagSinal(Regs[Reg1]);//atualiza flag de sinal
    }
    
};

class TosSub extends Assembly {
    //Subtrai reg com reg (resultado no primeiro reg)
    //Sub A,B
    public static final int Codigo = 6;
    
    public void Executa() {
        int Reg1=Controlador(PC);
        PC++;
        int Reg2=Controlador(PC);
        PC++;
        
        Regs[Reg1]-=Regs[Reg2];
        AtualizaFlagZero(Regs[Reg1]);//atualiza flag de zero
        AtualizaFlagSinal(Regs[Reg1]);//atualiza flag de zero
    }
    
};

class TosOr extends Assembly {
    //Or lógico de reg com reg
    //Or A,B
    public static final int Codigo = 7;
    
    public void Executa() {
        int Reg1=Controlador(PC);
        PC++;
        int Reg2=Controlador(PC);
        PC++;
        
        Regs[Reg1]=(Regs[Reg1] | Regs[Reg2]);
        AtualizaFlagZero(Regs[Reg1]);//atualiza flag de zero
        AtualizaFlagSinal(Regs[Reg1]);//atualiza flag de sinal
    }
    
};

class TosAnd extends Assembly {
    //And lógico de reg com reg (resultado no flag de zero)
    //And A,B
    public static final int Codigo = 8;
    
    public void Executa() {
        int Reg1=Controlador(PC);
        PC++;
        int Reg2=Controlador(PC);;
        PC++;
        
        Regs[Reg1]=(Regs[Reg1] & Regs[Reg2]);
        AtualizaFlagZero(Regs[Reg1]);//atualiza flag de zero
        AtualizaFlagSinal(Regs[Reg1]);//atualiza flag de sinal
    }
    
};

class TosCmp extends Assembly {
    // Compara reg com reg (resultado no flag de zero)
    //Cmp A,B
    public static final int Codigo = 9;
    
    public void Executa() {
        int Reg1=Controlador(PC);
        PC++;
        int Reg2=Controlador(PC);
        PC++;
        
        FlagZero=(Regs[Reg1]==Regs[Reg2]);
    }
    
};

class TosLdi extends Assembly {
    //carrega conteúdo de reg com a posicao de memoria
    //Ldi A,2000
    public static final int Codigo = 10;
    
    public void Executa() {
        int Reg=Controlador(PC);
        PC++;
        int End=Controlador(PC);
        PC++;
        
        Regs[Reg]=Memoria.Acessar(End);
    }
    
};

class TosSti extends Assembly {
    //guarda conteúdo de reg em memoria
    //Sti A,2000
    public static final int Codigo = 11;
    
    public void Executa() {
        int Reg=Controlador(PC);
        PC++;
        int End=Controlador(PC);
        PC++;
        
        Memoria.Gravar(End, Regs[Reg]);
    }
};

class TosJump extends Assembly {
    //Jump para posição de memoria
    public static final int Codigo = 12;
    public static final int Padrao = 0;
    public static final int NaoZero = 1;
    public static final int Zero = 2;
    
    
    public void Executa() {
        int Tipo=Controlador(PC);
        PC++;
        int End=Controlador(PC);
        PC++;
        
        if(Tipo==Padrao) //Jumper incondicional
            //PC=Memoria.Acessar(End);
            PC=End;
        else if(Tipo==NaoZero) //Jump se não deu zero
        {
            if(FlagZero==false) PC=End;
        } else if(Tipo==Zero)//Jump se deu zero
        {
            if(FlagZero==true) PC=End;
        }
    }
    
};

class TosNop extends Assembly {
    //Nada
    public static final int Codigo = 13;
    
    public void Executa() {
        
    }
    
};

class TosCall extends Assembly {
    //Jumper q salva a posição para retorno
    //Call 2000
    public static final int Codigo = 14;
    
    public void Executa() {
        int End=Controlador(PC);
        //PC=Memoria.Acessar(End);
        Memoria.Empilha(PC+1);//empilha endereço para retorno
        PC=End;
    }
};

class TosRet extends Assembly {
    //Jumper q retorna ao último Call
    //Ret
    public static final int Codigo = 15;
    
    public void Executa() {
        PC=Memoria.Desempilha();//desempilha endereço de retorno
    }
    
};


class TosPush extends Assembly {
    //Guarda reg na pilha
    //Push A
    public static final int Codigo = 16;
    
    public void Executa() {
        int Reg=Controlador(PC);
        PC++;
        Memoria.Empilha(Regs[Reg]);//empilha o conteúdo do reg
    }
    
};

class TosPop extends Assembly {
    //Joga conteúdo da pilha pro reg
    //Pop A
    public static final int Codigo = 17;
    
    public void Executa() {
        int Reg=Controlador(PC);
        PC++;
        Regs[Reg]=Memoria.Desempilha();//desempilha para o reg
    }
};

class TosHalt extends Assembly {
    //Para o programa
    public static final int Codigo = 18;
    
    public void Executa() {
        PC=-100;
    }
    
};

class TosStr extends Assembly {
    //guarda conteúdo de reg em memoria unsando acesso indireto
    //Str A,B (Destino, Origem)
    public static final int Codigo = 19;
    
    public void Executa() {
        int Reg1=Controlador(PC);
        PC++;
        int Reg2=Controlador(PC);
        PC++;
        
        Memoria.Gravar(Regs[Reg2], Regs[Reg1]);
    }
};

class TosLdr extends Assembly {
    //carrega conteúdo de reg com a posicao de memoria usando acesso indireto
    //Ldr A,B (Destino, Origem)
    public static final int Codigo = 20;
    
    public void Executa() {
        int Reg1=Controlador(PC);
        PC++;
        int Reg2=Controlador(PC);
        PC++;
        
        Regs[Reg1]=Memoria.Acessar(Regs[Reg2]);
    }
};

/**
 * Exceção causada pelo compilador do JPACS
 */
class JPACSCompilerException extends Exception {
    public JPACSCompilerException(String Msg) {
        super(Msg);
    }
};