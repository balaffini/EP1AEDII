package com.company;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

class ArvoreB {
    private static final int NULL = -1;
    private static final int TRUE = 1;
    private static final int FALSE = 0;
    private static final int TAMANHO_CABECALHO = 4;
    private String nomeArq;
    private No raiz;

    private int t;   //2t-1 = maximo de chaves
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

        @Override
        public String toString(){
            String saida = "No:";
            for (int i = 0; i < nChaves; i++) {
                saida += " " + chaves[i];
            }
            saida += "\n";

            if(!ehFolha()){
                saida += "Filhos:\n";
                for (int i = 0; i < nChaves + 1; i++) {
                    try {
                        No f = leNo(filhos[i]);
                        for (int j = 0; j < f.nChaves; j++) {
                            saida += f.chaves[j] + " ";
                        }
                        saida += "\n";
                    } catch(IOException ignored){}
                }
                saida += "\n";
            }
            return saida;
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

    boolean novoNo(int k) throws IOException {
        No r = this.raiz;

        if(raiz.estaCheio()){
            No s = new No(false); //nova raiz
            s.nChaves = 0;
            s.filhos[0] = r.endereco;
            gravaNovoNo(s);
            trocaRaiz(s);
            split(s, 0, r);
            return novoNoNaoCheio(s, k);
        }
        else
            return novoNoNaoCheio(r, k);
    }

    boolean novoNoNaoCheio(No x, int k) throws IOException {
        int i = x.nChaves-1;

        if(x.ehFolha()){
            while(i >= 0 && k < x.chaves[i]) {
                x.chaves[i+1] = x.chaves[i];
                i--;
            }
            x.chaves[i+1] = k;
            x.nChaves++;
            atualizaNo(x);
        }
        else{
            while (i >= 0 && k < x.chaves[i])
                i--;
            i++;
            No y = leNo(x.filhos[i]);
            if(y.estaCheio()){
                split(x, i, y);
                if(k > x.chaves[i]) { //?
                    i++;
                    y = leNo(x.filhos[i]);
                }
            }
            return novoNoNaoCheio(y, k);
        }
        return true;
    }

    boolean split(No x, int i, No y) throws IOException {
        No z = new No(y.ehFolha());
        z.nChaves = (t-1);
        y.nChaves = (t-1);

        for (int j = 0; j < t-1; j++) {
            z.chaves[j] = y.chaves[j+t];
        }
        if(!y.ehFolha()){
            for (int j = 0; j < t; j++) {
                z.filhos[j] = y.filhos[j+t];
            }
        }
        gravaNovoNo(z);

        for (int j = x.nChaves; j > i; j--) {
            x.filhos[j+1] = x.filhos[j];
        }
        x.filhos[i+1] = z.endereco;

        for (int j = x.nChaves; j > i; j--) {
            x.chaves[j] = x.chaves[j-1];
        }
        x.chaves[i] = y.chaves[t-1];
        x.nChaves++;

        atualizaNo(x);
        atualizaNo(y);

        return true;
    }

    boolean removeRaiz(No x, int k) throws IOException {
        if(x.ehFolha()){ //teoricamente errado mas se pa que ta certo
            return apaga(x, k);
        }
        No b = busca(x, k);
        int f = posFilho(x, b);
        if (x.endereco == b.endereco || f >= 0) {
            if (b.ehFolha()) {
                if (b.nChaves <= t - 1) {
                    mergeIr(x, b, f);
                }
                apaga(b, k);
            }
            else {
                if(b.nChaves < t){
                    mergeIr(x, b, f);
                    x = checaRaiz(x);
                    removeRaiz(x, k);
                }
                else {
                    int j = achaPos(b, k);
                    No y = fpre(leNo(b.filhos[j]));
                    No z = fsuc(leNo(b.filhos[j + 1]));
                    if (y.nChaves > t - 1) {
                        int k_ = y.chaves[y.nChaves - 1];
                        move(b, k_, k);
                        apaga(y, k_);
                        atualizaNo(y);
                    } else if (z.nChaves > t - 1) {
                        int k_ = z.chaves[0];
                        move(b, k_, k);
                        apaga(z, k_);
                        atualizaNo(z);
                    } else {
                        mergea(b, y, z, j, 0);
                        apaga(y, k);
                    }
                }
            }
        }
        else{
            int i = x.nChaves-1;
            while(i > 0 && k < x.chaves[i])
                i--;
            No n = leNo(x.filhos[i]);
            if(n.nChaves < t){
                mergeIr(x, n, i);
            }
            return removeRaiz(n, k);
        }
        return true;
    }

    boolean mergeIr(No x, No b, int f) throws IOException {
        No ir;
        if (f < x.nChaves) {
            ir = leNo(x.filhos[f + 1]);
            if(ir.nChaves >= t){
                mergeaa(x, b, ir, f, 0);
            }
            else{
                mergea(x, b, ir, f, 0);
            }
        }
        else {
            ir = leNo(x.filhos[f - 1]);
            if(ir.nChaves >= t){
                mergeaa(x, b, ir, f, ir.nChaves-1);
            }
            else{
                mergea(x, b, ir, f, ir.nChaves-1);
            }
        }
        return true;
    }

    boolean mergeaa(No x, No y, No z, int pos, int q) throws IOException {
        y.chaves[y.nChaves] = x.chaves[pos];
        x.chaves[pos] = z.chaves[q];
        y.nChaves++;
        apaga(z, z.chaves[q]);
        return true;
    }

    boolean mergea(No x, No y, No z, int pos, int q) throws IOException {
        y.chaves[y.nChaves] = x.chaves[pos];
        y.nChaves++;
        merge(x, y, z);
        apaga(x, x.chaves[pos]);
        return true;
    }

    No checaRaiz(No x) throws IOException {
        if(raiz.nChaves == 0){
            try{
                No r = leNo(raiz.filhos[0]);
                if(r.nChaves > 0){
                    trocaRaiz(r);
                    return r;
                }
            } catch (NullPointerException ignored){}
        }
        return x;
    }

    int posFilho(No x, No b){
        int i = 0;
        while(i <= x.nChaves && x.filhos[i] != b.endereco){
            i++;
        }
        if(i > x.nChaves)
            return -1;
        return i;
    }

    boolean apaga(No x, int k) throws IOException { //sempre eh folha
        int i = x.nChaves-1;
        while (i > 0 && k < x.chaves[i])
            i--;
        while (i < x.nChaves-1){
            x.chaves[i] = x.chaves[i+1];
            i++;
        }
        x.nChaves--;
        atualizaNo(x);
        return true;
    }

    int achaPos(No x, int k){
        int i = x.nChaves-1;
        while (x.chaves[i] != k)
            i--;
        return i;
    }

    boolean merge(No x, No y, No z) throws IOException { //errado
        int i = 0;
        while(i < z.nChaves){
            y.chaves[y.nChaves + i] = z.chaves[i];
            i++;
        }
        if(!y.ehFolha()){
            int l = 0;
            while(l < z.nChaves){
                y.filhos[y.nChaves + l] = z.filhos[l];
                l++;
            }
        }
        y.nChaves += z.nChaves;
        int j = 0;
        while(x.filhos[j] != z.endereco){
            j++;
        }
        while(j < x.nChaves){
            x.filhos[j] = x.filhos[j+1];
            j++;
        }
        atualizaNo(x);
        atualizaNo(y);
        return true;
    }

    boolean move(No x, int k_, int k){
        x.chaves[achaPos(x, k)] = k_;
        return true;
    }

    No fpre(No x) throws IOException {
        while(!x.ehFolha()){
            x = leNo(x.filhos[x.nChaves]);
        }
        return x;
    }

    No fsuc(No x) throws IOException {
        while(!x.ehFolha()){
            x = leNo(x.filhos[0]);
        }
        return x;
    }

    boolean buscaa(int k) throws IOException {
        return busca(raiz, k) != null;
    }

    boolean remove(int k) throws IOException {
        removeRaiz(raiz, k);
        checaRaiz(raiz);
        return true;
    }

    public void print() throws IOException {
        imprime(raiz);
    }

    public void imprime(No no) throws IOException {
        System.out.print("No: ");
        for (int i = 0; i < no.nChaves; i++) {
            System.out.print(no.chaves[i] + " ");
        }
        System.out.println();
        if(!no.ehFolha()){
            System.out.println("Filhos:");
            for (int i = 0; i < no.nChaves + 1; i++) {
                No f = leNo(no.filhos[i]);
                imprime(f);
            }
            System.out.println();
        }
    }

    @Override
    public String toString(){
        String saida = "Raiz:";
        for (int i = 0; i < raiz.nChaves; i++) {
            saida += " " + raiz.chaves[i];
        }
        saida += "\n";

        if(!raiz.ehFolha()){
            saida += "Filhos:\n";
            for (int i = 0; i < raiz.nChaves+1; i++) {
                try {
                    No f = leNo(raiz.filhos[i]);
                    for (int j = 0; j < f.nChaves; j++) {
                        saida += f.chaves[j] + " ";
                    }
                    saida += "\n";
                } catch(IOException ignored){}
            }
            saida += "\n";
        }
        return saida;
    }
}

public class EP1 {
    public static void main(String[] args) throws IOException {

        int testes[] = {100, 1000, 10000, 100000, 1000000};
        int ts[] = {2, 50, 100, 150, 200, 250, 300, 1000};

        for (int k = 0; k < ts.length; k++) {
            String nome = "arvoreTestes" + ts[k] + ".txt";
            ArvoreB a = new ArvoreB(nome, ts[k]);

            for (int i = 0; i < testes.length; i++) {
                long t0 = System.currentTimeMillis();
                for (int j = 0; j < testes[i]; j++) {
                    a.novoNo(j);
                }
                float tTotal = System.currentTimeMillis() - t0;
                tTotal /= 1000;
                System.out.println(testes[i] + " testes para t = " + ts[k]);
                System.out.println("Insercao: " + tTotal);
                System.out.println();
            }
            for (int i = 0; i < testes.length; i++) {
                long t0 = System.currentTimeMillis();
                for (int j = 0; j < testes[i]; j++) {
                    a.buscaa(j);
                }
                float tTotal = System.currentTimeMillis() - t0;
                tTotal /= 1000;
                System.out.println(testes[i] + " testes para t = " + ts[k]);
                System.out.println("Busca: " + tTotal);
                System.out.println();
            }
            for (int i = 0; i < testes.length; i++) {
                long t0 = System.currentTimeMillis();
                for (int j = 0; j < testes[i]; j++) {
                    a.remove(j);
                }
                float tTotal = System.currentTimeMillis() - t0;
                tTotal /= 1000;
                System.out.println(testes[i] + " testes para t = " + ts[k]);
                System.out.println("Remocao: " + tTotal);
                System.out.println();
            }
        }
    }
}