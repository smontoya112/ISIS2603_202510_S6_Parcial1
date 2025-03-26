package co.edu.uniandes.dse.parcial1.services;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import co.edu.uniandes.dse.parcial1.entities.EstacionEntity;
import co.edu.uniandes.dse.parcial1.entities.RutaEntity;
import co.edu.uniandes.dse.parcial1.exceptions.EntityNotFoundException;
import co.edu.uniandes.dse.parcial1.services.EstacionService;
import co.edu.uniandes.dse.parcial1.services.RutaEstacionService;
import co.edu.uniandes.dse.parcial1.services.RutaService;
import jakarta.transaction.Transactional;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

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

	private EstacionEntity estacion = new EstacionEntity();
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
			entityManager.persist(rutaEntity);
            entityManager.persist(estacionEntity);
            rutaList.add(rutaEntity);
            estacionList.add(estacionEntity);

		}
	}
    @Test
    void testAddEstacion() throws Exception {
        EstacionEntity nuevaEstacion = factory.manufacturePojo(EstacionEntity.class);
        entityManager.persist(nuevaEstacion);
        EstacionEntity estacionVieja = rutaEstacionService.addEstacionRuta(nuevaEstacion.getId(), ruta.getId());
        
        assertNotNull(estacionVieja);
        assertEquals(nuevaEstacion.getId(), estacionVieja.getId());
        assertEquals(nuevaEstacion.getName(), estacionVieja.getName());
        assertEquals(nuevaEstacion.getDireccion(), estacionVieja.getDireccion());
    }
    @Test
    void testAddEstacionInvalida() {
        assertThrows(EntityNotFoundException.class, () -> {
			EstacionEntity estacion1 = factory.manufacturePojo(EstacionEntity.class);
			estacion1.setRutas(List.of(ruta)
            );
			entityManager.persist(estacion1);
			rutaEstacionService.addEstacionRuta(estacion1.getId(),0L);
		});
    }
    @Test
    void testAddEstacionRutaInvalida() {
        assertThrows(EntityNotFoundException.class, () -> {
			RutaEntity ruta1 = factory.manufacturePojo(RutaEntity.class);
			ruta1.setEstaciones(List.of(estacion));
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

    
        EstacionEntity estacion2 = factory.manufacturePojo(EstacionEntity.class);
        estacion2.setRutas(List.of(ruta));
        entityManager.persist(estacion2);
        rutaEstacionService.addEstacionRuta(estacion2.getId(),ruta.getId());
        rutaEstacionService.removeEstacionRuta(estacion2.getId(),ruta.getId());
        assertNull(ruta.getEstaciones().stream().filter(e->e.getId().equals(estacion2.getId())).findFirst().orElse(null));
    }
    @Test
    void testRemoveEstacionInvalidaRuta()  {
        
        assertThrows(EntityNotFoundException.class, () -> {
			RutaEntity ruta1 = factory.manufacturePojo(RutaEntity.class);
			ruta1.setEstaciones(List.of(estacion));
			entityManager.persist(ruta1);
			rutaEstacionService.removeEstacionRuta( 0L, ruta1.getId());
		});
    }
    @Test
    void testRemoveEstacionRutaInvalida () {
        assertThrows(EntityNotFoundException.class, () -> {
			EstacionEntity estacion2 = factory.manufacturePojo(EstacionEntity.class);
			estacion2.setRutas(List.of(ruta));
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
