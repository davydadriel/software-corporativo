package exemplo.jpa.test;

import exemplo.jpa.Categoria;
import exemplo.jpa.DatasLimite;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author MASC
 */
public class JpqlTest {
    private static EntityManagerFactory emf;
    private static final Logger LOGGER = Logger.getGlobal();
    private EntityManager em;
    
    public JpqlTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        LOGGER.setLevel(Level.INFO);
        emf = Persistence.createEntityManagerFactory("exemplo_11");
        DbUnitUtil.inserirDados();
    }
    
    @AfterClass
    public static void tearDownClass() {
        emf.close();
    }
    
    @Before
    public void setUp() {
        em = emf.createEntityManager();
    }
    
    @After
    public void tearDown() {
        em.close();
    }

    @Test
    public void categoriaPorNome() {
        TypedQuery<Categoria> query = em.createQuery(
                "SELECT c FROM Categoria c WHERE c.nome LIKE :nome ORDER BY c.id", 
                Categoria.class);
        query.setParameter("nome", "Instrumentos%");
        List<Categoria> categorias = query.getResultList();
        
        for (Categoria categoria : categorias) {
            assertTrue(categoria.getNome().startsWith("Instrumentos"));
        }        
    }
    
    @Test
    public void categoriaPorNome2() {
        TypedQuery<Categoria> query = em.createNamedQuery("Categoria.PorNome", Categoria.class);
        query.setParameter("nome", "Instrumentos%");
        List<Categoria> categorias = query.getResultList();
        
        for (Categoria categoria : categorias) {
            assertTrue(categoria.getNome().startsWith("Instrumentos"));
        }        
    }
    
    @Test
    public void categoriasQuantidadeFilhas() {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Categoria c WHERE c.mae IS NOT NULL", Long.class);
        Long resultado = query.getSingleResult();        
        assertEquals(new Long(3), resultado);
    }    

    @Test
    public void maximaMinimaDataNascimento() {
        Query query = em.createQuery(
                "SELECT MAX(c.dataNascimento), MIN(c.dataNascimento) FROM Comprador c");
        Object[] resultado = (Object[]) query.getSingleResult();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String maiorData = dateFormat.format((Date)resultado[0]);
        String menorData = dateFormat.format((Date)resultado[1]);
        assertEquals("21-12-1999", maiorData);
        assertEquals("11-08-1973", menorData);
    }    

    @Test
    public void datasLimite() {
        TypedQuery<DatasLimite> query = em.createQuery(
                "SELECT NEW exemplo.jpa.DatasLimite(MAX(c.dataNascimento), MIN(c.dataNascimento)) FROM Comprador c", 
                DatasLimite.class);
        DatasLimite datasLimite = query.getSingleResult();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String dataMaxima = dateFormat.format((Date)datasLimite.getDataMaxima());
        String dataMinima = dateFormat.format((Date)datasLimite.getDataMinima());
        assertEquals("21-12-1999", dataMaxima);
        assertEquals("11-08-1973", dataMinima);
    }    

}
