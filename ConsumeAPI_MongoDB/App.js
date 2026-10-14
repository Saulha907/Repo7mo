import { useEffect, useState } from 'react';
import Constants from 'expo-constants';
import { StatusBar } from 'expo-status-bar';
import {
  ActivityIndicator,
  FlatList,
  Image,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
//primero que cargue la aplicion, tiene que modificar cuando le des click a cualquier de las cartidas te de que aparezca un modal para que puedas agregar esa pelicula
const expoHost = Constants.expoConfig?.hostUri;
const MOVIES_API_URL =
  process.env.EXPO_PUBLIC_MOVIES_API_URL ||
  (expoHost ? `http://${expoHost}/api/movies` : 'http://localhost:4000/movies');

export default function App() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedMovie, setSelectedMovie] = useState(null);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);

    const timeout = setTimeout(() => {
      controller.abort();
      setError('The movie server did not respond within 15 seconds. Reload the app to try again.');
      setLoading(false);
    }, 15000);

    const loadMovies = async () => {
      try {
        const response = await fetch(MOVIES_API_URL, {
          signal: controller.signal,
        });

        if (!response.ok) {
          throw new Error(`Could not load movies (HTTP ${response.status}).`);
        }

        const data = await response.json();

        if (!Array.isArray(data)) {
          throw new Error('The movies API must return an array.');
        }

        if (!controller.signal.aborted) {
          setMovies(data);
        }
      } catch (error) {
        if (!controller.signal.aborted) {
          setError(error.message);
        }
      } finally {
        clearTimeout(timeout);
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };

    loadMovies();

    return () => {
      clearTimeout(timeout);
      controller.abort();
    };
  }, []);

  if (loading) {
    return (
      <View style={styles.loader}>
        <ActivityIndicator size="large" color="#007AFF" />
        <StatusBar style="auto" />
      </View>
    );
  }

  const renderItem = ({ item }) => (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={`Ver detalles de ${item.title}`}
      accessibilityHint="Abre la ficha completa de la película"
      onPress={() => setSelectedMovie(item)}
      style={({ pressed }) => [styles.card, pressed && styles.cardPressed]}
    >
      {item.poster ? (
        <Image
          source={{ uri: item.poster }}
          style={styles.poster}
          resizeMode="cover"
        />
      ) : (
        <View style={[styles.poster, styles.noposter]}>
          <Text>Sin imagen</Text>
        </View>
      )}
      <View style={styles.info}>
        <Text style={styles.title}>{item.title}</Text>
        <Text style={styles.plot}>{item.fullplot || 'Sin descripción'}</Text>
      </View>
    </Pressable>
  );

  return (
    <View style={styles.container}>
      {error ? (
        <Text style={styles.list}>{error}</Text>
      ) : (
        <FlatList
          data={movies}
          keyExtractor={(item) => item._id}
          renderItem={renderItem}
          contentContainerStyle={styles.list}
          ListEmptyComponent={<Text>No movies loaded.</Text>}
        />
      )}
      <Modal
        animationType="fade"
        transparent
        visible={Boolean(selectedMovie)}
        statusBarTranslucent
        onRequestClose={() => setSelectedMovie(null)}
      >
        <Pressable
          accessibilityLabel="Cerrar detalles de la película"
          onPress={() => setSelectedMovie(null)}
          style={styles.modalBackdrop}
        >
          {selectedMovie ? (
            <Pressable
              accessibilityViewIsModal
              onPress={(event) => event.stopPropagation()}
              style={styles.modalCard}
            >
              <ScrollView
                contentContainerStyle={styles.modalContent}
                showsVerticalScrollIndicator={false}
              >
                {selectedMovie.poster ? (
                  <Image
                    source={{ uri: selectedMovie.poster }}
                    style={styles.modalPoster}
                    resizeMode="cover"
                  />
                ) : (
                  <View style={[styles.modalPoster, styles.noposter]}>
                    <Text>Sin imagen</Text>
                  </View>
                )}
                <Text style={styles.modalTitle}>{selectedMovie.title}</Text>
                <Text style={styles.modalPlot}>
                  {selectedMovie.fullplot || 'Sin descripción'}
                </Text>
                <Pressable
                  accessibilityRole="button"
                  accessibilityLabel="Cerrar"
                  onPress={() => setSelectedMovie(null)}
                  style={({ pressed }) => [
                    styles.closeButton,
                    pressed && styles.closeButtonPressed,
                  ]}
                >
                  <Text style={styles.closeButtonText}>Cerrar</Text>
                </Pressable>
              </ScrollView>
            </Pressable>
          ) : null}
        </Pressable>
      </Modal>
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F3F6F8',
  },
  loader: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#F3F6F8',
  },
  list: {
    paddingHorizontal: 16,
    paddingTop: 56,
    paddingBottom: 24,
  },
  card: {
    flexDirection: 'row',
    marginBottom: 14,
    padding: 10,
    borderRadius: 16,
    backgroundColor: '#FFFFFF',
    shadowColor: '#1F3542',
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.08,
    shadowRadius: 10,
    elevation: 3,
  },
  cardPressed: {
    opacity: 0.9,
    transform: [{ scale: 0.98 }],
  },
  poster: {
    width: 100,
    height: 140,
    borderRadius: 10,
  },
  noposter: {
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#E4EBEF',
  },
  info: {
    flex: 1,
    justifyContent: 'center',
    marginLeft: 14,
  },
  plot: {
    fontSize: 13,
    lineHeight: 19,
    color: '#667680',
  },
  title: {
    fontSize: 18,
    lineHeight: 22,
    fontWeight: '700',
    marginBottom: 8,
    color: '#173B4C',
  },
  modalBackdrop: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 20,
    paddingVertical: 40,
    backgroundColor: 'rgba(10, 28, 37, 0.76)',
  },
  modalCard: {
    width: '100%',
    maxWidth: 420,
    maxHeight: '90%',
    borderRadius: 24,
    backgroundColor: '#FFFFFF',
    shadowColor: '#07151C',
    shadowOffset: {
      width: 0,
      height: 12,
    },
    shadowOpacity: 0.25,
    shadowRadius: 24,
    elevation: 12,
  },
  modalContent: {
    alignItems: 'center',
    padding: 20,
  },
  modalPoster: {
    width: 200,
    height: 300,
    borderRadius: 14,
    backgroundColor: '#E4EBEF',
  },
  modalTitle: {
    marginTop: 18,
    fontSize: 24,
    lineHeight: 29,
    fontWeight: '700',
    textAlign: 'center',
    color: '#173B4C',
  },
  modalPlot: {
    alignSelf: 'stretch',
    marginTop: 12,
    fontSize: 15,
    lineHeight: 23,
    color: '#526772',
  },
  closeButton: {
    alignItems: 'center',
    justifyContent: 'center',
    alignSelf: 'stretch',
    minHeight: 48,
    marginTop: 20,
    borderRadius: 14,
    backgroundColor: '#173B4C',
  },
  closeButtonPressed: {
    opacity: 0.88,
    transform: [{ scale: 0.96 }],
  },
  closeButtonText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFFFFF',
  },
});
