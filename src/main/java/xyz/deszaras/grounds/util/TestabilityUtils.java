package xyz.deszaras.grounds.util;

/**
 * Methods to help with writing tests.
 */
public class TestabilityUtils {

  private TestabilityUtils() {
  }

  // https://stackoverflow.com/questions/33628585/getclass-of-mockito-mock
  private static final String MOCKITO_MOCK = "$MockitoMock$";

  /**
   * Given a class that might represent a (Mockito) mock, get the class being
   * mocked.
   *
   * @param clazz class that is potentially a mock
   * @return non-mock class
   */
  public static Class<?> nonmock(Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    while (clazz.getSimpleName().contains(MOCKITO_MOCK)) {
      clazz = clazz.getSuperclass();
    }
    return clazz;
  }
}
