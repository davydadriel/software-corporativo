package exemplo.jpa.test;

import exemplo.jpa.CartaoCredito;
import exemplo.jpa.Categoria;
import exemplo.jpa.Comprador;
import exemplo.jpa.DatasLimite;
import exemplo.jpa.Usuario;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 *
 * @author MASC
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JpqlTest {

    private static EntityManagerFactory emf;
    private static final Logger logger = Logger.getGlobal();
    private EntityManager em;

    public JpqlTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        logger.setLevel(Level.INFO);
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
    public void t01_categoriaPorNome() {
        logger.info("Executando t01: SELECT c FROM Categoria c WHERE c.nome LIKE :nome ORDER BY c.id");
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
    public void t02_categoriaPorNome() {
        logger.info("Executando t02: Categoria.PorNome");
        TypedQuery<Categoria> query = em.createNamedQuery("Categoria.PorNome", Categoria.class);
        query.setParameter("nome", "Instrumentos%");
        List<Categoria> categorias = query.getResultList();

        for (Categoria categoria : categorias) {
            assertTrue(categoria.getNome().startsWith("Instrumentos"));
        }
    }

    @Test
    public void t03_quantidadeCategoriasFilhas() {
        logger.info("Executando t03: SELECT COUNT(c) FROM Categoria c WHERE c.mae IS NOT NULL");
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Categoria c WHERE c.mae IS NOT NULL", Long.class);
        Long resultado = query.getSingleResult();
        assertEquals(new Long(3), resultado);
    }

    @Test
    public void t04_maximaEMinimaDataNascimento() {
        logger.info("Executando t04: SELECT MAX(c.dataNascimento), MIN(c.dataNascimento) FROM Comprador c");
        Query query = em.createQuery(
                "SELECT MAX(c.dataNascimento), MIN(c.dataNascimento) FROM Comprador c");
        Object[] resultado = (Object[]) query.getSingleResult();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String maiorData = dateFormat.format((Date) resultado[0]);
        String menorData = dateFormat.format((Date) resultado[1]);
        assertEquals("21-12-1999", maiorData);
        assertEquals("11-08-1973", menorData);
    }

    @Test
    public void t05_maximaEMinimaDataNascimento() {
        logger.info("Executando t05: SELECT NEW exemplo.jpa.DatasLimite(MAX(c.dataNascimento), MIN(c.dataNascimento)) FROM Comprador c");
        TypedQuery<DatasLimite> query = em.createQuery(
                "SELECT NEW exemplo.jpa.DatasLimite(MAX(c.dataNascimento), MIN(c.dataNascimento)) FROM Comprador c",
                DatasLimite.class);
        DatasLimite datasLimite = query.getSingleResult();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String dataMaxima = dateFormat.format((Date) datasLimite.getDataMaxima());
        String dataMinima = dateFormat.format((Date) datasLimite.getDataMinima());
        assertEquals("21-12-1999", dataMaxima);
        assertEquals("11-08-1973", dataMinima);
    }

    @Test
    public void t06_categoriasMaes() {
        logger.info("Executando t06: SELECT c FROM Categoria c WHERE c.filhas IS NOT EMPTY");
        TypedQuery<Categoria> query;
        query = em.createQuery(
                "SELECT c FROM Categoria c WHERE c.filhas IS NOT EMPTY",
                Categoria.class);
        List<Categoria> categorias = query.getResultList();

        for (Categoria categoria : categorias) {
            assertTrue(!categoria.getFilhas().isEmpty());
        }
    }

    @Test
    public void t07_compradoresVisa() {
        logger.info("Executando t07: SELECT c FROM Comprador c WHERE c.cartaoCredito.bandeira like ?1 ORDER BY c.nome DESC");
        TypedQuery<Comprador> query;
        query = em.createQuery(
                "SELECT c FROM Comprador c WHERE c.cartaoCredito.bandeira like ?1 ORDER BY c.nome DESC",
                Comprador.class);
        query.setParameter(1, "VISA"); //Setando parâmetro posicional.
        query.setMaxResults(20); //Determinando quantidade máxima de resultados.
        List<Comprador> compradores = query.getResultList();

        for (Comprador comprador : compradores) {
            assertEquals("VISA", comprador.getCartaoCredito().getBandeira());

        }
    }

    @Test
    public void t08_compradoresVisaMastercard() {
        logger.info("Executando t08: SELECT c FROM Comprador c WHERE c.cartaoCredito.bandeira LIKE ?1 OR c.cartaoCredito.bandeira LIKE ?2 ORDER BY c.nome DESC");
        TypedQuery<Comprador> query;
        query = em.createQuery(
                "SELECT c FROM Comprador c "
                + "WHERE c.cartaoCredito.bandeira LIKE ?1 "
                + "OR c.cartaoCredito.bandeira LIKE ?2 ORDER BY c.nome DESC",
                Comprador.class);
        query.setParameter(1, "VISA"); //Setando parâmetro posicional.
        query.setParameter(2, "MASTERCARD"); //Setando parâmetro posicional.        
        List<Comprador> compradores = query.getResultList();

        for (Comprador comprador : compradores) {
            switch (comprador.getCartaoCredito().getBandeira()) {
                case "VISA":
                    assertTrue(true);
                    break;
                case "MASTERCARD":
                    assertTrue(true);
                    break;
                default:
                    assertTrue(false);
                    break;
            }
        }
    }

    @Test
    public void t09_compradoresMastercardMaestro() {
        logger.info("Executando t09: SELECT c FROM Comprador c WHERE c.cartaoCredito.bandeira IN ('MAESTRO', 'MASTERCARD') ORDER BY c.nome DESC");
        TypedQuery<Comprador> query;
        query = em.createQuery(
                "SELECT c FROM Comprador c "
                + "WHERE c.cartaoCredito.bandeira IN ('MAESTRO', 'MASTERCARD') ORDER BY c.nome DESC",
                Comprador.class);
        List<Comprador> compradores = query.getResultList();

        for (Comprador comprador : compradores) {
            switch (comprador.getCartaoCredito().getBandeira()) {
                case "MAESTRO":
                    assertTrue(true);
                    break;
                case "MASTERCARD":
                    assertTrue(true);
                    break;
                default:
                    assertTrue(false);
                    break;
            }
        }
    }

    @Test
    public void t10_usuariosPorDataNascimento() {
        logger.info("Executando t10: SELECT u FROM Usuario u WHERE u.dataNascimento BETWEEN ?1 AND ?2");
        TypedQuery<Usuario> query;
        query = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.dataNascimento BETWEEN ?1 AND ?2",
                Usuario.class);
        query.setParameter(1, getData(21, Calendar.FEBRUARY, 1980));
        query.setParameter(2, getData(1, Calendar.DECEMBER, 1990));
        List<Usuario> usuarios = query.getResultList();

        for (Usuario usuario : usuarios) {
            System.out.println(usuario);
        }
    }

    private Date getData(Integer dia, Integer mes, Integer ano) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, ano);
        c.set(Calendar.MONTH, mes);
        c.set(Calendar.DAY_OF_MONTH, dia);
        return c.getTime();
    }

    @Test
    public void t11_categoriaMaePorFilha() {
        logger.info("Executando t11: SELECT c FROM Categoria c WHERE :categoria MEMBER OF c.filhas");
        Categoria categoria = em.find(Categoria.class, new Long(2));
        TypedQuery<Categoria> query;
        query = em.createQuery(
                "SELECT c FROM Categoria c WHERE :categoria MEMBER OF c.filhas",
                Categoria.class);
        query.setParameter("categoria", categoria);
        categoria = query.getSingleResult();
        assertEquals("Instrumentos Musicais", categoria.getNome());
    }

    /**
     * Consulta por cartões de crédito expirados.
     * JPQL: SELECT c FROM CartaoCredito c WHERE c.dataExpiracao < CURRENT_TIMESTAMP
     */
    @Test
    public void t12_cartoesExpirados() {
        logger.info("Executando t12: SELECT c FROM CartaoCredito c WHERE c.dataExpiracao < CURRENT_TIMESTAMP");
        TypedQuery<CartaoCredito> query = em.createQuery("SELECT c FROM CartaoCredito c WHERE c.dataExpiracao < CURRENT_TIMESTAMP", CartaoCredito.class);
        List<CartaoCredito> cartoesExpirados = query.getResultList();        
        assertEquals(1, cartoesExpirados.size());
    }
}