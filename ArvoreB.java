/*
package com.company;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ArvoreB {
    private static final int NULL = -1;
    private static final int TRUE = 1;
    private static final int FALSE = 0;
    private static final int TAMANHO_CABECALHO = 4;
    private String nomeArq;
    private No raiz;

    private int t;   //maximo de chaves
    private int TAMANHO_NO_INTERNO;
    private int TAMANHO_NO_FOLHA;
    private int NUM_MAX_CHAVES;
    private int NUM_MAX_FILHOS;

    public ArvoreB(String nomeArq, int t) throws IOException {
        this.nomeArq = nomeArq;
        inicializaConstantes(t);

        File f = new File(nomeArq);
        if (!f.exists()) {
            No no = new No(true);
            no.endereco = TAMANHO_CABECALHO;  //primeiro noh
            trocaRaiz(no);
            atualizaNo(no);
        }
        else
            carregaRaizNaRAM();
    }

    private class No {
        private int folha;
        int nChaves;
        int endereco; //localizacao por n de bytes
        int chaves[] = new int[NUM_MAX_CHAVES]; //valores de cada no
        int filhos[]; //pq nao NO filhos[]? //filhos contem os valores pra acessar depois?

        No(boolean ehFolha) {
            if(ehFolha)
                folha = TRUE;
            else {
                folha = FALSE;
                filhos = new int[NUM_MAX_FILHOS];
                Arrays.fill(filhos, NULL);
            }
        }

        boolean ehFolha() {
            return folha == TRUE;
        }

        boolean estaCheio() {
            return nChaves == NUM_MAX_CHAVES;
        }

        void imprime() {
            System.out.println("Eh folha: " + (folha == TRUE));
            System.out.println("nChaves: " + nChaves);
            System.out.print("Chaves:");

            for (int i = 0; i < nChaves; i++) {
                System.out.print(" " + chaves[i]);
            }
            System.out.println();

            if (!ehFolha()) {
                System.out.print("Endereco dos filhos:");

                for (int i = 0; i < nChaves + 1; i++) {
                    System.out.print(" " + filhos[i]);
                }
                System.out.println();
            }
        }
    }

    private void inicializaConstantes(int t) {
        this.t = t;
        TAMANHO_NO_INTERNO = 12 * (2 * t - 1) + (8 * t);
        TAMANHO_NO_FOLHA = 12 * (2 * t - 1);
        NUM_MAX_CHAVES = 2 * t - 1;
        NUM_MAX_FILHOS = NUM_MAX_CHAVES + 1;
    }

    private void trocaRaiz(No novaRaiz) throws FileNotFoundException, IOException {
        RandomAccessFile arq = new RandomAccessFile(nomeArq, "rw");
        this.raiz = novaRaiz;
        arq.writeInt(this.raiz.endereco);
        arq.close();
    }

    private void carregaRaizNaRAM() throws FileNotFoundException, IOException {
        RandomAccessFile arq = new RandomAccessFile(nomeArq, "r");
        this.raiz = leNo(arq.readInt());
        arq.close();
    }

    private No leNo(int endereco) throws IOException {
        RandomAccessFile arq = new RandomAccessFile(nomeArq, "r");

        if (arq.length() == 0 || endereco == NULL) {
            arq.close();
            return null;
        }

        arq.seek(endereco);
        boolean ehFolha = (arq.readInt() == TRUE);

        byte bytes[];
        if(ehFolha)
            bytes = new byte[TAMANHO_NO_FOLHA - 4];
        else
            bytes = new byte[TAMANHO_NO_INTERNO - 4];

        arq.read(bytes);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);

        No no = new No(ehFolha);
        no.nChaves = leInt(in);
        no.endereco = endereco;

        for (int i = 0; i < no.chaves.length; i++) {
            no.chaves[i] = leInt(in);
        }

        if (!ehFolha) {
            for (int i = 0; i < no.filhos.length; i++) {
                no.filhos[i] = leInt(in);
            }
        }

        arq.close();
        return no;
    }

    // assume que o "no" ja tem um endereco
    private void atualizaNo(No no) throws IOException {
        int nBytes;
        if(no.ehFolha())
            nBytes = TAMANHO_NO_FOLHA;
        else
            nBytes = TAMANHO_NO_INTERNO;

        ByteArrayOutputStream out = new ByteArrayOutputStream(nBytes);
        escreveInt(out, no.folha);
        escreveInt(out, no.nChaves);

        for (int i = 0; i < no.chaves.length; i++) {
            escreveInt(out, no.chaves[i]);
        }

        if (!no.ehFolha()) {
            for (int i = 0; i < no.filhos.length; i++) {
                escreveInt(out, no.filhos[i]);
            }
        }

        RandomAccessFile arq = new RandomAccessFile(nomeArq, "rw");
        arq.seek(no.endereco);
        arq.write(out.toByteArray());
        arq.close();
    }

    // apos chamar a funcao, o "no" terah um endereco
    private void gravaNovoNo(No no) throws IOException {
        // obs 1: "new File" nao cria um arquivo novo, apenas carrega
        // informacoes sobre o arquivo ja existente
        // obs 2: o novo no sera gravado no final do arquivo
        // (ou seja, vai aumentar o tamanho do arq qdo chamar "gravaNovoNo")
        no.endereco = (int) new File(nomeArq).length();
        atualizaNo(no);
    }

    private int leInt(ByteArrayInputStream in) {
        byte[] bInt = new byte[4];
        in.read(bInt, 0, 4);
        return ByteBuffer.wrap(bInt).asIntBuffer().get();
    }

    private void escreveInt(ByteArrayOutputStream out, int i) {
        byte[] num = ByteBuffer.allocate(4).putInt(i).array();
        out.write(num, 0, 4);
    }

    int busca(int buscado, String nomeArq) throws IOException {
        RandomAccessFile file = new RandomAccessFile(nomeArq, "r");
        return buscaBinaria(buscado, 0, (int)file.length()-1, file);
    }

    int buscaBinaria(int buscado, int ini, int fim, RandomAccessFile file){
        if(fim < ini)
            return -1;
        int meio = (ini+fim)/2;
        //if()
        return 0;
    }

    No busca(No x, int k) throws IOException{ //x eh a raiz e k o buscado
        int i = 0;

        while (i < x.nChaves && k > x.chaves[i])
            i++;
        if(i < x.nChaves && k == x.chaves[i])
            return x;

        if(x.ehFolha())
            return null;
        else
            return busca(leNo(x.filhos[i]), k);
    }

    boolean novoNo(ArvoreB a, int k) throws IOException {
        No r = a.raiz;

        if(raiz.estaCheio()){
            No s = new No(false); //nova raiz
            a.raiz = s;
            s.nChaves = 0;
            s.filhos[0] = r.endereco; //conferir endereco talvez seja chave
            split(s, 0, r);
            return novoNoNaoCheio(s, k);
        }
        else
            return novoNoNaoCheio(r, k);
    }

    boolean novoNoNaoCheio(No x, int k) throws IOException {
        int i = x.nChaves-1;//conferir n-1

        if(x.ehFolha()){
            while(i > 0 && k < x.chaves[i]) { //conferir >0 e o i
                x.chaves[i] = x.chaves[i-1];
                i--;
            }
            x.chaves[i] = k; //conferir i
            x.nChaves++;
            gravaNovoNo(x);
        }
        else{
            while(i > 0 && k < x.chaves[i]) //conferir >0
                i--;
            i++;
            No y = leNo(x.filhos[i]); //NAO SEI
            if(y.estaCheio()){
                split(x, i, y);
                if(k > x.chaves[i]) { //conferir i
                    i++;
                    y = leNo(x.filhos[i]);
                }
                return novoNoNaoCheio(y, k);
            }
        }
        return true; //?
    }

    boolean split(No x, int i, No y) throws IOException {

        No z = new No(y.ehFolha());
        z.nChaves = (t-1);
        y.nChaves = (t-1);

        for (int j = 0; j < t-2; j++) { //conferir o -2
            z.chaves[j] = y.chaves[j+t];
        }
        if(!y.ehFolha()){
            for (int j = 0; j < t-1; j++) { //conferir o -1
                z.filhos[j] = y.filhos[j+t];
            }
        }

        for (int j = x.nChaves-1; j > i; j--) { //conferir o > i e o nchaves-1
            x.filhos[j] = x.filhos[j-1];
        }
        x.filhos[i+1] = z.endereco; //conferir esse .endereco e o i+1
        for (int j = x.nChaves-1; j > i-1; j--) { //conferir nchaves-1 e i-1
            x.chaves[j] = x.chaves[j-1]; //conferir j e j-1
        }
        x.chaves[i] = y.chaves[i];
        x.nChaves++;

        gravaNovoNo(y); //geranovo?
        gravaNovoNo(z);
        gravaNovoNo(x);

        return true;
    }


    // DICA 1:
    // Onde vc gostaria de fazer isso: "no1.filhos[i] = no2",
    // faca isso: "no1.filhos[i] = no2.endereco"
    // ATENCAO: se o no for recem nascido, antes de acessar
    // o campo "endereco", chame gravaNovoNo(no);

    // DICA 1.2 (esqueci de falar disso no video :P):
    // Onde vc gostaria de fazer isso: "metodo(no.filhos[i])"
    // ou isso "No no2 = no1.filhos[i]", faca isso:
    // "metodo(leNo(no.filhos[i]))" ou "No no2 = leNo(no1.filhos[i])"

    // DICA 2:
    // No split, apenas um novo noh eh criado. Portanto, chame a funcao
    // "gravaNovoNo" apenas uma vez no split. Os outros dois nos envolvidos
    // ja existiam antes da chamada, entao basta chamar "atualizaNo"

    // DICA 3:
    // Toda vez que for fazer split na raiz, voce tera que criar uma nova
    // raiz antes de chamar o split, certo? Antes de chamar o split na raiz,
    // chame "gravaNovoNo" na nova raiz que voce criou e em seguida chame
    // "trocaRaiz(novaRaiz)".

    public static void main(String[] args) throws IOException {
        ArvoreB a = new ArvoreB("arvre.txt", 10);
        No no1 = novoNo(a, 2);
    }
}

/*public class EP1 {
    //public static void main(String[] args) throws IOException {
    //    ArvoreB a = new ArvoreB("arvre.txt", 10);
    //}
    public static void main(String[] args) throws IOException {
        ArvoreB arvB = new ArvoreB("arvore_b.txt", 10);
        No no = arvB.get(false);

        for(int i = 0; i < 5; i++) {
            no.chaves[i] = i;
            no.nChaves++;
        }

        arvB.gravaNo(no, 0);

        arvB.leNo(0).imprime();
    }
}*/