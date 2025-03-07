import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import co.edu.uniandes.dse.parcial1.entities.EstacionEntity;
import co.edu.uniandes.dse.parcial1.entities.RutaEntity;
import co.edu.uniandes.dse.parcial1.services.EstacionService;
import co.edu.uniandes.dse.parcial1.services.RutaEstacionService;
import co.edu.uniandes.dse.parcial1.services.RutaService;
import uk.co.jemos.podam.api.PodamFactory;

@DataJpaTest
@Transactional
@Import({RutaService.class, EstacionService.class,RutaEstacionService.class})
public class RutaEstacionServiceTest {
    @Autowired
    private RutaEstacionService rutaEstacionService;
    
    @Autowired
    private RutaService rutaService;

    @Autowired
    private EstacionService estacionService;

    @Autowired
	private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

	private RutaEntity ruta = new RutaEntity();
	private List<RutaEntity> rutaList = new ArrayList<>();

	private EstacionService estacion = new EstacionService();
	private List<EstacionEntity> estacionList = new ArrayList<>();
    @BeforeEach
	void setUp() {
		clearData();
		insertData();
	}
    private void clearData() {
		entityManager.getEntityManager().createQuery("delete from EstacionEntity").executeUpdate();
		entityManager.getEntityManager().createQuery("delete from RutaEntity").executeUpdate();
	}
    private void insertData() {
		ruta = factory.manufacturePojo(RutaEntity.class);
		entityManager.persist(ruta);

		estacion = factory.manufacturePojo(EstacionEntity.class);
		entityManager.persist(estacion);

		for (int i = 0; i < 3; i++) {
            RutaEntity rutaEntity = factory.manufacturePojo(RutaEntity.class);
            EstacionEntity estacionEntity = factory.manufacturePojo(EstacionEntity.class);
			entityManager.persist(rutaEntity, estacionEntity);
            rutaList.add(rutaEntity);
            estacionList.add(estacionEntity);

		}
	}
    @Test
    void testAddEstacion() {
        EstacionEntity nuevaEstacion = factory.manufacturePojo(EstacionEntity.class);
        entityManager.persist(nuevaEstacion);
        EstacionEntity estacionVieja = rutaEstacionService.addEstacionRuta(nuevaEstacion.getId(), ruta.getId());
        
        assertNotNull(estacionVieja);
        assertEquals(nuevaEstacion.getId(), estacionVieja.getId());
        assertEquals(nuevaEstacion.getNombre(), estacionVieja.getNombre());
        assertEquals(nuevaEstacion.getDireccion(), estacionVieja.getDireccion());
    }
    @Test
    void testAddEstacionInvalida() {
        assertThrows(EntityNotFoundException.class, () -> {
			EstacionEntity estacion1 = factory.manufacturePojo(EstacionEntity.class);
			estacion1.setRutas(ruta);
			entityManager.persist(estacion1);
			rutaEstacionService.addEstacionRuta(estacion1.getId(),0L);
		});
    }
    @Test
    void testAddEstacionRutaInvalida() {
        assertThrows(EntityNotFoundException.class, () -> {
			RutaEntity ruta1 = factory.manufacturePojo(RutaEntity.class);
			ruta1.setEstaciones(estacion);
			entityManager.persist(ruta1);
			rutaEstacionService.addEstacionRuta( 0L, ruta1.getId());
		});
    }
    @Test
    void testAddEstacionRutaCircular() {
        assertThrows(Exception.class, ()->{
            RutaEntity ruta1 = factory.manufacturePojo(RutaEntity.class);
            RutaEntity ruta2 = factory.manufacturePojo(RutaEntity.class);
            RutaEntity ruta3 = factory.manufacturePojo(RutaEntity.class);
            ruta.setTipo("circular");
            ruta1.setTipo("circular");
            ruta2.setTipo("circular");
            
            EstacionEntity estacion1 = factory.manufacturePojo(EstacionEntity.class);
            estacion1.setRutas(List.of(ruta1, ruta2, ruta3));
            estacion1.setCapacidad(10);
            entityManager.persist(estacion1);
            rutaEstacionService.addEstacionRuta(estacion1.getId(), ruta.getId());
        });
    }

    @Test
    void testRemoveEstacionRuta () throws Exception {

        EstacionEntity estacion1 = factory.manufacturePojo(EstacionEntity.class);
        EstacionEntity estacionVieja = estacionService.createEstacion(estacion1);
        rutaEstacionService.addEstacionRuta(estacionVieja.getId(), ruta.getId());
        rutaEstacionService.removeEstacionRuta(estacionVieja.getId(), ruta.getId());
        assertEquals(0, rutaEstacionService.getEstacionesRuta(ruta.getId()).size());
   
    }
    @Test
    void testRemoveEstacionInvalidaRuta()  {
        
        assertThrows(EntityNotFoundException.class, () -> {
			RutaEntity ruta1 = factory.manufacturePojo(RutaEntity.class);
			ruta1.setEstaciones(estacion);
			entityManager.persist(ruta1);
			rutaEstacionService.removeEstacionRuta( 0L, ruta1.getId());
		});
    }
    @Test
    void testRemoveEstacionRutaInvalida () {
        assertThrows(EntityNotFoundException.class, () -> {
			EstacionEntity estacion2 = factory.manufacturePojo(EstacionEntity.class);
			estacion2.setRutas(ruta);
			entityManager.persist(estacion2);
			rutaEstacionService.addEstacionRuta(estacion2.getId(),0L);
		});
    }
    @Test
    void testRemoveEstacionRutaMenosDeUnaRutaNocturna ()  {
        assertThrows(Exception.class, ()->{
            RutaEntity ruta1 = factory.manufacturePojo(RutaEntity.class);
            RutaEntity ruta2 = factory.manufacturePojo(RutaEntity.class);
            RutaEntity ruta3 = factory.manufacturePojo(RutaEntity.class);
            ruta.setTipo("circular");
            ruta1.setTipo("circular");
            ruta2.setTipo("circular");
            
            EstacionEntity estacion2 = factory.manufacturePojo(EstacionEntity.class);
            estacion2.setRutas(null);
            estacion2.setRutas(List.of(ruta1, ruta2, ruta3));
            entityManager.persist(estacion2);
            rutaEstacionService.removeEstacionRuta(estacion2.getId(), ruta.getId());
        });
    }
} 
